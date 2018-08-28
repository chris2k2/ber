const puppeteer = require('puppeteer');
let cheerio = require('cheerio');
let request = require('async-request');
let dateFormat = require('dateformat');

exports.tournaments = all_tournaments;
exports.full_overview = full_overview;
exports.default_type = determineType;


// is tested separately because it does a callback to turnier.de
exports.get_type = get_tl_type;

async function full_overview(url) {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();

    await page.setRequestInterception(true);
    page.on('request', request => {
        if (request.resourceType() === 'image')
            request.abort();
        else
            request.continue();
    });

    console.log("... requesting uri");
    await page.goto(url, {
        waitUntil: 'networkidle0'
    });
    console.log("... requested uri");
    //await page.screenshot({fullPage:true, path:"screenshot.png"});

    const renderedContent = await page.evaluate(() => new XMLSerializer().serializeToString(document));
    browser.close();

    return renderedContent;
}

async function all_tournaments(html, type) {
    var list = [];
    var $ = cheerio.load(html);

    console.log("Start crawling");
    $("#searchResultArea").each(function(index1, element1) {
        $(element1).find('.media__link').each(function (index, element) {


            var link = $(element).attr("href");
            if (!link.startsWith("https://")) {
                link = "https://www.turnier.de" + link;
            }

            var id = link.replace("https://www.turnier.de", "");
            id = id.replace("sport/tournament.aspx?id=", "");
            id = id.replace("/", "");

            // 01.01.2018 bis 02.01.2018 -> ['02', '01', '2018']
            let date = $(element1).find('.dates').eq(0).text().replace(/.*bis/, '').trim();
            let dateObject = Date.parse(date);

            var current = {};
            current.id = id;
            current.name = $(element).text();
            current.setType = type(current, link);
            current.status = "UNPROCESSED";
            current.source = "turnierde";
            current.endDate = dateFormat(dateObject, 'yyyy-mm-dd');

            console.log("... found " + JSON.stringify(current));
            list.push(current);
        });
    });

    console.log("... determining type");
    await Promise.all(list.map(async entry => entry.setType));

    console.log("... done. Crawled Tournaments: " + list.length);

    return list;
}


async function determineType(current, url) {
    var response = await request(url);
    var result = get_tl_type(response.body);

    current.type = result;
    delete current.setType;
}

function get_tl_type(html) {
    let $ = cheerio.load(html);

    if ($(".monthcalendar").length > 0) {
        return "league";
    }
    else {
        return "tournament";
    }

};
