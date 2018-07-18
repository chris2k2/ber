let cheerio = require('cheerio');
var request = require('request-repeat');
var utils = require('../utils');
let dateFormat = require('dateformat');
let moment = require('moment');

exports.scrap = start;

var requestWithDefaults = request.defaults({
    json: true,
    retry: {
        max: 5,
        backoffBase: 1000,
        backoffExponent: 3,
        retryOn5xx: true,
        errorFn: function (request, error, retries) {
            if (retries === 4) {
                console.error(`- Request to ${request.url} failed on the ${retries} attempt with error ${error.message}`);
            }
        },
        successFn: function (req, res) {
            console.info(`- Got status-code ${res.statusCode} on request to ${request.url}`);
        }
    }
});


function getDiscipline(discipline) {
    let newDisc = discipline.replace(/[0-9]./, '')
    newDisc = newDisc.replace("GD", "MX").replace("HD", "MD").replace("HE", "MS").replace("DD", "WD").replace("DE", "WS");
    return newDisc.replace(/[0-9]+/, "");
}

function getTrimedFromIndex(cheerioElement, index) {
    return cheerioElement.find('td').eq(index).text().trim();
}

function getPlayers($, element, index) {
    var players = [];
    $(element).find('td').eq(index).each(function (i2, playerscol) {

        $(playerscol).contents().each(function (i3, playerscolchild) {
            var text = $(playerscolchild).text().trim();
            var newPlayer = {};
            if (text.length > 0) {
                newPlayer.name = text;
                if (playerscolchild.type == "tag") {
                    newPlayer.id = $(playerscolchild).attr("href").split("/").filter(word => word.search('-') >= 0)[0];
                }

                if (newPlayer.id == null || newPlayer.id == "") {
                    newPlayer.id = utils.name2id(newPlayer.name);
                }

                players.push(newPlayer);
            }
        });
    });

    return players;
}

function match(match, tournament, callback) {
    console.log("starting match crawl: " + match.url);

    let html = requestWithDefaults.get(match.url).then(function (html) {
        var $ = cheerio.load(html.body);

        var matches = [];
        $('table.liga-main-table tr').each(function (index, element) {

            var discipline = getTrimedFromIndex($(element), 0);
            var matchresult = getTrimedFromIndex($(element), 4);
            var homePlayers = getPlayers($, element, 1);
            var awayPlayers = getPlayers($, element, 3);

            if (discipline.length > 0) {
                matches.push(
                    {
                        "league": $('[id=liga-sub-content] h1').text(),
                        "hometeam": $('table.liga-main-table th').eq(1).text().trim(),
                        "awayteam": $('table.liga-main-table th').eq(3).text().trim(),
                        "discipline": getDiscipline(discipline),
                        "homePlayers": homePlayers,
                        "awayPlayers": awayPlayers,
                        "result": matchresult,
                        "source": "nuliga-bbv",
                        "leaguedepth": utils.getLeagueDepthByName(tournament.name),
                        "league": tournament.name,
                        "leagueid": tournament.id,
                        "date": match.date
                    });
            }
        });

        callback(matches);
    });
}

async function league(url, tournament, callback) {
    console.log("starting league crawl: " + url);
    let html = await requestWithDefaults.get(url);

    var $ = cheerio.load(html.body);

    var matches = [];
    $('table.meeting-table tbody tr').each(function (index, element) {
        let dateText = $(element).find("td").eq(0).text().trim().replace(" ", "");
        let date = moment(dateText, 'DD.MM.YYYY HH:mm');
        $(element).find("td a").each(function (index2, element2) {
            if ($(element2).text() == "Details") {
                let match = {};
                match.date = dateFormat(date, "yyyy-mm-dd'T'hh:MM:ss");
                match.url = 'https://badminton-bbv.de' + $(element2).attr("href");
                matches.push(match);
            }
        });

    });

    let finished = true;
    $('td.align-right.score').each(function (index, element) {
        if ($(element).text() == "") {
            finished = false;
        }
    });

    for (let match of matches) {
        callback(match, tournament);
    }

    return finished;
}

function start(url, tournament, cb) {
    return league(url, tournament, (m, t) => match(m, t, cb));
}


