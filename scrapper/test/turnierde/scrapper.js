var expect = require("chai").expect;
var scrapper = require("../../app/turnierde/scrapper");
var fs = require("fs");

describe("Tournament Matches Parsing", function () {
    it("shouldCrawl parse a simple match", function (done) {
        fs.readFile('./scrapper/test/turnierde/matches_min.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var matches = scrapper.getMatchesFromTournament(fileContents);

            expect(matches.length).to.equal(1);
            expect(matches[0].league).to.equal("Pleinfelder Brombachseepokal 2018 (BAY)");
            expect(matches[0].date).to.equal("7. April 2018 09:15");
            expect(matches[0].homePlayers[0].name).to.equal("Johannes Leinfelder");
            expect(matches[0].homePlayers[1].name).to.equal("Victoria Löhr");
            expect(matches[0].awayPlayers[0].name).to.equal("Reiner Alberter");
            expect(matches[0].awayPlayers[1].name).to.equal("Uli Schmidt");
            expect(matches[0].result).to.equal("15-7 13-15 15-9");
            done();
        });
    });
});

describe("Tournament Checking", function () {
    it("shouldCrawl check that player played Augsbuger", function (done) {
        fs.readFile('./scrapper/test/turnierde/played.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.played(fileContents, "Augsburger Open 2018");

            expect(id).to.equal(true);
            done();
        });
    });


    it("shouldCrawl check that player didnt play Bitburger Open", function (done) {
        fs.readFile('./scrapper/test/turnierde/played.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.played(fileContents, "Bitburger Open");

            expect(id).to.equal(false);
            done();
        });
    });

    it("should check that it is not finished", function(done) {
        fs.readFile('./scrapper/test/turnierde/notfinished.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var matches = scrapper.getMatchesFromTournament(fileContents);
            var finished = scrapper.finished(matches);

            expect(finished).to.equal(false);
            done();
        });
    });
    it("should check that it is finished", function (done) {
        fs.readFile('./scrapper/test/turnierde/matches_min.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var matches = scrapper.getMatchesFromTournament(fileContents);
            var finished = scrapper.finished(matches);

            expect(finished).to.equal(true);
            done();
        });
    });
});

describe("Teammatch Parsing", function () {

    it("should parse a Bundesliga Match", function (done) {
        fs.readFile('./scrapper/test/turnierde/teammatch.html', 'utf8', function (err, fileContents) {
            var matches = scrapper.getMatchesFromTeammatch(fileContents);

            expect(matches.length).to.equal(7);
            expect(matches[2].league).to.equal("1. Bundesliga – (1. BL) – (001) 1. Bundesliga");
            expect(matches[2].date).to.equal("Sa 24.03.2018 14:00");
            expect(matches[2].homePlayers[0].name).to.equal("Roman Zirnwald");
            expect(matches[2].homePlayers[1].name).to.equal("Robert Blair");
            expect(matches[2].awayPlayers[0].name).to.equal("Alexander Roovers");
            expect(matches[2].awayPlayers[1].name).to.equal("Robin Tabeling");
            expect(matches[2].result).to.equal("11-7 15-14 9-11 11-5");
            expect(matches[2].hometeam).to.equal("SC Union Lüdinghausen");
            expect(matches[2].awayteam).to.equal("1.BV Mülheim");
            expect(matches[2].discipline).to.equal("HD");

            done();
        });
    });
});

describe("Get IDs", function() {
   it("should be able to get a simple id", function(done) {
       fs.readFile('./scrapper/test/turnierde/player.html', 'utf8', function (err, fileContents) {
           var ids = scrapper.getIds(fileContents);

           expect(ids.length).to.equal(4);
           //expect(ids).to.equal(["07-038114", "07-038113", "07-037224", "07-WeyermannMarina"]);

           done();
       });
   });
});
