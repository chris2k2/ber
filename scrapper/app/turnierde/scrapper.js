let cheerio = require('cheerio');
var request = require('request');
var HashSet = require('hashset');

exports.getMatchesFromTournament = getMatchesTournament;
exports.getMatchesFromTeammatch = getMatchesFromTeammatch;
exports.getId = getId;

function getMatchesTournament(html) {
    var $ = cheerio.load(html);

    var matches = [];
    $('table.ruler.matches > tbody > tr').each(function (index, element) {
        var homePlayers = getPlayers($, element, 3);
        var awayPlayers = getPlayers($, element, 5);

        var score = $(element).find('.score').text();
        if (score.includes("Kein Spiel")) {
            score = "";
        }
        matches.push(
            {
                "league": $('.title h3').text(),
                "hometeam": "",
                "awayteam": "",
                "discipline": "",
                "homePlayers": homePlayers,
                "awayPlayers": awayPlayers,
                "result": score
            });
    });

    return matches;
};

function getId(html) {
    var $ = cheerio.load(html);

    let rows = $('table.ruler tbody tr');

    var ids = new HashSet();
    rows.each(function (index, row) {
        let col = $(row).find("td");
        let newId = col.eq(1).text().trim().split("-").map(x => x.replace(/^0+/, '')).join("-");
        ids.add(newId);
    });

    return ids.toArray();
};


exports.played = function (html, tournament) {
    var $ = cheerio.load(html);

    var playedTournament = false;
    $('table.ruler td a').each(function (index, element) {
        if ($(element).text() == tournament) {
            playedTournament = true;
        }
    });

    return playedTournament;
};


function getPlayers($, element, index) {
    var players = [];
    $(element).find('> td').eq(index).each(function (i2, playerscol) {

        $(playerscol).find('a').each(function (i3, playerscolchild) {
            var text = $(playerscolchild).text().trim();
            var newPlayer = {};
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
    var $ = cheerio.load(html);

    var hometeam, awayteam;
    $('table.ruler.matches thead tr').each(function (index, element) {
        hometeam = $(element).find("td").eq(1).text().replace(/[ ][(].*[)]/, "");
        awayteam = $(element).find("td").eq(3).text().replace(/[ ][(].*[)]/, "");
    });

    var date = $('div#content td').eq(0).text();

    var matches = [];
    $('table.ruler.matches > tbody > tr').each(function (index, element) {
        var homePlayers = getPlayers($, element, 1);
        var awayPlayers = getPlayers($, element, 3);

        var score = $(element).find('.score').text();
        if (score.includes("Kein Spiel")) {
            score = "";
        }

        var discipline = $(element).find("td").eq(0).text();
        discipline = discipline.replace(/\d/, "");

        matches.push(
            {
                "league": $('.title h3').text(),
                "date": date,
                "hometeam": hometeam,
                "awayteam": awayteam,
                "discipline": discipline,
                "homePlayers": homePlayers,
                "awayPlayers": awayPlayers,
                "result": score
            });
    });

    return matches;
}



