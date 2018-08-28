let cheerio = require('cheerio');
var request = require('request-repeat');
var utils = require('../utils');
let dateFormat = require('dateformat');

exports.start = all;

var requestWithDefaults = request.defaults({
    json: true,
    retry: {
        max: 5,
        backoffBase: 1000,
        backoffExponent: 1.5,
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

async function all(year) {
    let url = 'https://badminton-bbv.de/site/liga/ligen/' + year + '/';
    let endYear = '20' + year.replace(/.*[/]/, "");

    console.log("starting nuliga crawl: " + url);
    let html = await requestWithDefaults.get(url);

    var $ = cheerio.load(html.body);

    var allLeagues = [];
    $('.liga-col a').each(function (index, element) {
        let tournament = {};

        tournament.id = $(element).attr("href");
        tournament.type = "league";
        tournament.status = "UNPROCESSED";
        tournament.source = "nuliga-bbv";
        tournament.name = $(element).text();
        tournament.endDate = endYear + "-03-31";

        allLeagues.push(tournament);
    });

    return allLeagues;
}
