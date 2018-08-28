var expect = require("chai").expect;
var crawler = require("../../app/turnierde/crawler");
var fs = require("fs");

describe("Crawler", function () {

    it("can extract tournament ids", function(done)
    {
        fs.readFile('./scrapper/test/turnierde/tournamentoverview.html', 'utf8', function (err, fileContents) {
            crawler.tournaments(fileContents, async x => "tournament").then(list => {

                expect(list[0].id).to.equal("D5EFCDC5-8066-4E38-AEB7-9AF221AAA9F7");
                expect(list[0].name).to.equal("2. Kreis-ERLT U11-U15 NRW-Bezirk Süd 2 West 2011-12");
                expect(list[0].status).to.equal("UNPROCESSED");
                expect(list[0].source).to.equal("turnier.de");
                expect(list[0].endDate).to.equal("2018-06-22");

                done();
            });
        });
    });

    it("knows it is a league", function(done)
    {
        fs.readFile('./scrapper/test/turnierde/league.html', 'utf8', function (err, fileContents) {
            var res = crawler.get_type(fileContents);

            expect(res).to.equal("league");

            done();
        });
    });
    it("knows it is a tournament", function(done)
    {
        fs.readFile('./scrapper/test/turnierde/tournament.html', 'utf8', function (err, fileContents) {
            var res = crawler.get_type(fileContents);

            expect(res).to.equal("tournament");

            done();
        });
    });
});