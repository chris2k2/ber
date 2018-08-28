let cheerio = require('cheerio');
let request = require('request');
let HashSet = require('hashset');

exports.getMatchesFromTournament = getMatchesTournament;
exports.getMatchesFromTeammatch = getMatchesFromTeammatch;
exports.getIds = getIds;
exports.played = played;
exports.finished = finished;

function getMatchesTournament(html) {
    let $ = cheerio.load(html);

    let date = $('caption').text().replace(/.*[,]/, "");
    date = date.replace(/\n|\t/g, "").trim();

    let matches = [];
    $('table.ruler.matches > tbody > tr').each(function (index, element) {
        let homePlayers = getPlayers($, element, 3);
        let awayPlayers = getPlayers($, element, 5);

        let score = $(element).find('.score').text();
        if (score.includes("Kein Spiel")) {
            score = "";
        }

        let time = $(element).find('.plannedtime').text();
        matches.push(
            {
                "league": $('.title h3').text(),
                "hometeam": null,
                "awayteam": null,
                "discipline": null,
                "date": date + " " + time,
                "homePlayers": homePlayers,
                "awayPlayers": awayPlayers,
                "result": score
            });
    });

    return matches;
};

function getIds(html) {
    let $ = cheerio.load(html);

    let rows = $('#searchResultArea a');

    let ids = new HashSet();
    rows.each(function (index, row) {
        let newId = $(row).attr("href");
        //let col = $(row).find("td");
        //let newId = col.eq(1).text().trim().split("-").map(x => x.replace(/^0+/, '')).join("-");
        ids.add(newId);
    });

    return ids.toArray();
};


function played(html, tournament) {
    let $ = cheerio.load(html);

    let playedTournament = false;
    $("#tabcontent .media__link").each(function (index, element) {
        if ($(element).text() == tournament) {
            playedTournament = true;
        }
    });

    return playedTournament;
};


function getPlayers($, element, index) {
    let players = [];
    $(element).find('> td').eq(index).each(function (i2, playerscol) {

        $(playerscol).find('a').each(function (i3, playerscolchild) {
            let text = $(playerscolchild).text().trim();
            let newPlayer = {};
            if (text.length > 0) {
                newPlayer.name = text.replace(/\[.*\]/, '').trim();
                newPlayer.id = $(playerscolchild).attr("href");
                players.push(newPlayer);
            }
        });
    });

    return players;
}


function getMatchesFromTeammatch(html) {
    let $ = cheerio.load(html);

    let hometeam, awayteam;
    $('table.ruler.matches thead tr').each(function (index, element) {
        hometeam = $(element).find("td").eq(1).text().replace(/[ ][(].*[)]/, "");
        awayteam = $(element).find("td").eq(3).text().replace(/[ ][(].*[)]/, "");
    });

    let date = $('div#content td').eq(0).text();
    let league = $("div#content > table").eq(0).find("td").eq(1).text();

    let matches = [];
    $('table.ruler.matches > tbody > tr').each(function (index, element) {
        let homePlayers = getPlayers($, element, 1);
        let awayPlayers = getPlayers($, element, 3);

        let score = $(element).find('.score').text();
        if (score.includes("Kein Spiel")) {
            score = "";
        }

        let discipline = $(element).find("td").eq(0).text();
        discipline = discipline.replace(/\d/, "");

        matches.push(
            {
                "league": league,
                "date": date,
                "hometeam": hometeam,
                "awayteam": awayteam,
                "discipline": discipline,
                "homePlayers": homePlayers,
                "awayPlayers": awayPlayers,
                "result": score,
                "leaguedepth": "",
                "league": "",
                "leagueid": id,
                "date": ""
            });
    });

    return matches;
}

function finished(matches) {
    let finished = true;

    for (match of matches) {
        if (match.result.length == "") {
            finished = false;
        }
    }

    return finished;
}



