const puppeteer = require('puppeteer');
let cheerio = require('cheerio');
let request = require('async-request');
let dateFormat = require('dateformat');

exports.tournaments = all_tournaments;

// is tested separately because it does a callback to turnier.de
exports.get_type = get_tl_type;

async function full_overview(baseUrl, {year, month}) {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();

    await page.setRequestInterception(true);
    page.on('request', request => {
        if (request.resourceType() === 'image')
            request.abort();
        else
            request.continue();
    });

    var start = dateFormat(new Date(year, month, 1), 'yyyymmdd');
    var end = dateFormat(new Date(year, month + 1, 1), 'yyyymmdd')

    var url = baseUrl + "&sd=" + start + "&ed=" + end;

    await page.goto(url);

    await page.evaluate("PageSelected(1, 10000);");
    await page.waitForNavigation();

    const renderedContent = await page.evaluate(() => new XMLSerializer().serializeToString(document));
    browser.close();

    return renderedContent;
}

async function all_tournaments(html, type) {
    var list = [];
    var $ = cheerio.load(html);

    $('.sporticon').each(function (index, element) {
        var current = {};

        var link = $(element).attr("href");
        if (!link.startsWith("https://")) {
            link = "https://www.turnier.de" + link;
        }

        var id = link.replace("https://www.turnier.de", "");
        id = id.replace("sport/tournament.aspx?id=", "");
        id = id.replace("/", "");
        current.id = id;
        current.setType = type(current, link);

        list.push(current);
    });

    await Promise.all(list.map(async entry => entry.setType));

    return list;
}


async function determineType(current, url) {
    var response = await request(url);
    var type = get_tl_type(response.body);

    current.type = type;
    delete current.setType;
}

function get_tl_type(html) {
    var $ = cheerio.load(html);

    if ($(".monthcalendar").length > 0) {
        return "league";
    }
    else {
        return "tournament";
    }
};
