var expect = require("chai").expect;
var crawler = require("../../app/turnierde/crawler");
var fs = require("fs");

describe("Crawler", function () {

    it("can extract tournament ids", function(done)
    {
        fs.readFile('./test/turnierde/tournamentoverview.html', 'utf8', function (err, fileContents) {
            crawler.tournaments(fileContents, async x => "tournament").then(list => {

                expect(list[0].id).to.equal("92E03003-BFA6-4AAB-B7BD-673989870E19");
                expect(list[1].id).to.equal("77D62F1F-ADC3-4D1E-8FBE-EBFDB10790E3");
                expect(list[2].id).to.equal("B0BCE058-8639-4B40-A678-613A556DCF08");
                expect(list[3].id).to.equal("1A3A5696-43B3-4D71-A155-CEC0A822F80D");
                expect(list[4].id).to.equal("107D0FC0-C153-4EAF-A39D-EBAECB424B16");
                expect(list[5].id).to.equal("FD17FD03-359B-46C7-AAF2-7705D8FD2EB2");
                expect(list[6].id).to.equal("1BF5995C-E30B-487E-94AF-C9FB9F4E7202");
                expect(list[7].id).to.equal("ACBE269B-097B-4F22-83B5-0730231B9338");
                expect(list[8].id).to.equal("36EAC246-781B-4AAA-8D28-22999AD030E7");
                expect(list[9].id).to.equal("D1B85734-B588-4CB2-9EC6-5DBB90F1CEC8");

                done();
            });
        });
    });

    it("knows it is a league", function(done)
    {
        fs.readFile('./test/turnierde/league.html', 'utf8', function (err, fileContents) {
            var res = crawler.get_type(fileContents);

            expect(res).to.equal("league");

            done();
        });
    });
    it("knows it is a tournament", function(done)
    {
        fs.readFile('./test/turnierde/tournament.html', 'utf8', function (err, fileContents) {
            var res = crawler.get_type(fileContents);

            expect(res).to.equal("tournament");

            done();
        });
    });
});