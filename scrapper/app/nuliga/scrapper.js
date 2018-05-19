let cheerio = require('cheerio');
var request = require('request');

function match(url, callback) {
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
                    players.push(newPlayer);
                }
            });
        });

        return players;
    }

    request(url, function (error, response, html) {
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);

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
                            "discipline": discipline.replace(/[0-9]./, '').replace("GD", "MX").replace("HD", "MD").replace("HE", "MS").replace("DD", "WD").replace("DE", "WS"),
                            "homePlayers": JSON.stringify(homePlayers),
                            "awayPlayers": JSON.stringify(awayPlayers),
                            "result": matchresult
                        });
                }
            });

            callback(matches);
        }
    });
}

function league(url, callback) {
    request(url, function (error, response, html) {
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);

            var matches = [];
            $('table.liga-main-table tr td a').each(function (index, element) {
                if ($(element).text() == "Details") {
                    matches.push('https://badminton-bbv.de' + $(element).attr("href"));
                }
            });

            for (let match of matches) { callback(match); }
        }
    });
}

function all(url, callback) {
    request(url, function (error, response, html) {
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);

            var allLeagues = [];
            $('.liga-col a').each(function (index, element) {
                var text = $(element).attr("href");
                allLeagues.push('https://badminton-bbv.de' + text );
            });

            for (let league of allLeagues) { callback(league); }
        }
    });
}

// this should be a unit test...
//match('https://badminton-bbv.de/site/liga/liga/17/18/23100/meeting/236337', console.log);
//league('https://badminton-bbv.de/site/liga/liga/17/18/23213', x => console.log(x));
all('https://badminton-bbv.de/site/liga/ligen/17/18/', x => league(x, y => match(y, console.log)));

