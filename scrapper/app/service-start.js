const crawler = require('./turnierde/crawler');
const scrapper = require('./turnierde/scrapper');
const nl_scrapper = require('./nuliga/scrapper');
const nl_crawler = require('./nuliga/crawler');
const dateFormat = require('dateformat');
const request = require('async-request');
const puppeteer = require('puppeteer');
const cheerio = require('cheerio');
const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-central-1'});

require('events').EventEmitter.prototype._maxListeners = 64;
const sqs = new AWS.SQS({apiVersion: '2012-11-05'});

exports.crawler = start_crawler;
exports.scrapper = start_scrapper;
exports.post = post;
exports.nuliga = start_nuliga_scrapper;
exports.nuliga_crawler = start_nuliga_crawler;

async function start_nuliga_crawler(date) {
    console.log("Crawling nuliga");

    let year = date.getFullYear() - 1999;
    let next = year + 1;
    let season = year + "/" + next;

    let tournaments = await nl_crawler.start(season);

    post(tournaments, "NewTournaments");

    return "DONE";
}

async function start_crawler(start_year, start_month) {
    console.log("Receiving crawl");
    var end_year = new Date().getFullYear();
    var end_month = new Date().getMonth();

    var current_year = start_year;
    var current_month = start_month;

    var start = dateFormat(new Date(start_year, start_month, '01'), 'yyyy-mm-dd');
    var enddate = dateFormat(new Date(), 'yyyy-mm-dd');

    console.log("... reaching out for turnier.de");
    await crawler.full_overview("https://www.turnier.de/find/tournament?StartDate=" + start + "&EndDate=" + enddate + "&page=10000")
        .then(content => crawler.tournaments(content, crawler.default_type))
        .then(resultList => post(resultList, "NewTournaments"))
        .catch(error => console.log(error));

    return "done";
}

function eachLink(html, callable) {
    let $ = cheerio.load(html);
    $(".ruler a").each(callable);
}

function eachTeamLink(html, callable) {
    let $ = cheerio.load(html);
    $(".teamname").each(callable);
}

async function getAllHtmlsFromLinks(drawMatchLinks, page) {
    let drawMatchesHtmls = [];
    for (let drawMatchLink of drawMatchLinks) {
        drawMatchesHtmls.push(await getFromUrl(page, "https://www.turnier.de/sport/" + drawMatchLink));
    }
    return drawMatchesHtmls;
}

async function start_scrapper(tournament) {
    console.log("Scrapping: " + JSON.stringify(tournament));
    let page = await init_puppeteer();

    let matches = [];
    if (tournament.type == 'league') {
        let html = await getFromUrl(page, "https://www.turnier.de/sport/events.aspx?id=" + tournament.id);
        var $ = cheerio.load(html);
        console.log("... league detected");

        let links = await listify(html, eachLink);
        let eventLinks = links
            .map(element => $(element).attr("href"))
            .filter(link => link.includes("event.aspx"));

        let eventHtmls = await getAllHtmlsFromLinks(eventLinks, page);
        console.log("... events found");

        let drawLinks = [];
        for (let eventHtml of eventHtmls) {
            drawLinks = drawLinks.concat(await listify(eventHtml, eachLink));
        }
        drawLinks = drawLinks
            .map(element => $(element).attr("href"))

        let drawLinks2 = links
            .map(element => $(element).attr("href"))
            .filter(link => link.includes("draw.aspx"));
        drawLinks = drawLinks.concat(drawLinks2);
        console.log("... draws found");


        let drawMatchLinks = drawLinks
            .map(link => link.replace("draw.aspx?id=" + tournament.id + "&draw=", ""))
            .map(drawNr => "drawmatches.aspx?id=" + tournament.id + "&draw=" + drawNr)
        let drawMatchesHtmls = await getAllHtmlsFromLinks(drawMatchLinks, page);

        let teamMatchesLinks = new Set();
        for (let drawMatchHtml of drawMatchesHtmls) {
            let tmp = await listify(drawMatchHtml, eachTeamLink);
            for (let teamMatchLink of tmp) {
                teamMatchesLinks = teamMatchesLinks.add($(teamMatchLink).attr("href"));
            }
        }
        console.log("... teammatches found");

        let teamMatchesHtmls = await getAllHtmlsFromLinks(teamMatchesLinks, page);

        for (let teamMatchHtml of teamMatchesHtmls) {
            matches = matches.concat(scrapper.getMatchesFromTeammatch(teamMatchHtml));
        }
        console.log("... found " + matches.length + " matches");
    }
    else if (tournament.type = 'tournament') {
        console.log("... tournament detected");

        // https://www.turnier.de/sport/matches.aspx?id=28E38EA3-437C-4103-9852-4C09E0D4448A
        let html = await getFromUrl(page, "https://www.turnier.de/sport/matches.aspx?id=" + tournament.id);
        var $ = cheerio.load(html);

        // mit Pupeteer alle Dates durchgehen
        let dateLinks = [];
        $(".tournamentcalendar a").each(function (index, element) {
            dateLinks.push($(element).attr("href"));
        });
        dateLinks = dateLinks.map(link => link.replace("/sport", ""));

        let matchHtmls = await getAllHtmlsFromLinks(dateLinks, page);

        // Matches parsen
        matches = matchHtmls.map(html => scrapper.getMatchesFromTournament(html))
            .reduce((a, b) => a.concat(b));

        console.log("... found " + matches.length + " matches");

        // Map mit Spielern aufbauen
        let player2ids = new Map();
        matches.map(match => match.homePlayers.concat(match.awayPlayers)) // [[p1,p2,p3,p4], [p1,p2,p5,p6]]
            .reduce((a, b) => a.concat(b))
            .map(player => player.name)
            .forEach(x => player2ids.set(x, null));

        console.log("... playerids filling...");

        // Suche aufrufen
        for (let name of player2ids.keys()) {
            let html = await getFromUrl(page, "https://www.turnier.de/find/player?q=" + name);
            let ids = scrapper.getIds(html);
            if (ids.length == 1) {
                let parsedId = ids[0].replace(/.*\//, "");
                player2ids.set(name, parsedId);
            }
            else if (ids.length > 1) {
                for (let link of ids) {
                    let playerhtml = await getFromUrl(page, "https://www.turnier.de" + link + "/tournaments");
                    let played = scrapper.played(playerhtml, tournament.name);
                    if (played) {
                        let parsedId = ids[0].replace(/.*\//, "");
                        player2ids.set(name, parsedId);
                    }
                }
            }
        }

        console.log("... playerids filled");
        let players = matches.map(match => match.homePlayers.concat(match.awayPlayers));
        let flatPlayers = [].concat.apply([], players);
        flatPlayers.forEach(p => p.id = player2ids.get(p.name));

        await post(matches, "NewMatches");

        if (scrapper.finished(matches)) {
            tournament.status = "DONE";
        }
        else {
            tournament.status = "ONGOING";
        }

        console.log("... tournament done. Status " + tournament.status);
        await post(tournament, "NewTournaments");

        return "done";
    }
}


async function post(content, queue) {
    if (!Array.isArray(content) || content.length > 0) {
        var params = {
            MessageBody: JSON.stringify(content),
            MessageAttributes: {
                'contentType': {
                    DataType: 'String',
                    StringValue: 'application/json'
                },
            },
            QueueUrl: "https://sqs.eu-central-1.amazonaws.com/261784908038/" + queue
        };

        sqs.sendMessage(params, function (err, data) {
            if (err) {
                console.log("Error", err);
            } else {
                console.log("Success", data.MessageId);
            }
        });
    }
}

async function getFromUrl(page, url) {
    await page.goto(url);

    return await page.evaluate(() => new XMLSerializer().serializeToString(document));
}

async function init_puppeteer(url) {
    var browser = await puppeteer.launch();
    var page = await browser.newPage();

    await page.setRequestInterception(true);
    page.on('request', request => {
        if (request.resourceType() === 'image')
            request.abort();
        else
            request.continue();
    });

    return page;
}


function listify(cookie, callee) {
    return new Promise(function (resolve, reject) {
        let list = [];
        let aggregatorFunction = function (index, element) {
            list.push(element);
        };

        callee(cookie, aggregatorFunction);

        resolve(list);
    });
}

function start_nuliga_scrapper(tournament) {
    nl_scrapper.scrap('https://badminton-bbv.de' + tournament.id, tournament, matches => post(matches, "NewMatches")).then(function (finished) {
        let tournaments = [];
        if (finished) {
            tournament.status = "DONE";
        } else {
            tournament.status = "ONGOING";
        }
        tournaments.push(tournament);
        return tournaments;
    }).then(ts => post(ts, "NewTournaments"));
}