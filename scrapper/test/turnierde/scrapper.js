var expect = require("chai").expect;
var scrapper = require("../../app/turnierde/scrapper");
var fs = require("fs");

describe("Tournament Matches Parsing", function () {
    it("shouldCrawl parse a simple match", function (done) {
        fs.readFile('./test/turnierde/matches_min.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var matches = scrapper.getMatchesFromTournament(fileContents);

            expect(matches.length).to.equal(1);
            expect(matches[0].league).to.equal("Pleinfelder Brombachseepokal 2018 (BAY)");
            expect(matches[0].homePlayers[0].name).to.equal("Johannes Leinfelder");
            expect(matches[0].homePlayers[1].name).to.equal("Victoria Löhr");
            expect(matches[0].awayPlayers[0].name).to.equal("Reiner Alberter");
            expect(matches[0].awayPlayers[1].name).to.equal("Uli Schmidt");
            expect(matches[0].result).to.equal("15-7 13-15 15-9");
            done();
        });
    });
});


describe("Id Parsing", function () {
    it("shouldCrawl parse a simple id", function (done) {
        fs.readFile('./test/turnierde/oneid.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.getId(fileContents);

            expect(id).to.have.members(["7-37224"]);
            done();
        });
    });

    it("shouldCrawl parse a simple id if twice same", function (done) {
        fs.readFile('./test/turnierde/sameids.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.getId(fileContents);

            expect(id).to.have.members(["7-37224"]);
            done();
        });
    });


    it("shouldCrawl separate different ids", function (done) {
        fs.readFile('./test/turnierde/differentids.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.getId(fileContents);

            expect(id).to.have.members(["7-37224", "7-123456"]);
            done();
        });
    });

});

describe("Tournament Checking", function () {
    it("shouldCrawl check that Christian played Zirndorf", function (done) {
        fs.readFile('./test/turnierde/playedpleinfeld.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.played(fileContents, "3.Zirndorfer Stadtmeisterschaft");

            expect(id).to.equal(true);
            done();
        });
    });


    it("shouldCrawl check that Christian didnt play Bitburger Open", function (done) {
        fs.readFile('./test/turnierde/playedpleinfeld.html', 'utf8', function (err, fileContents) {
            if (err) throw err;
            var id = scrapper.played(fileContents, "Bitburger Open");

            expect(id).to.equal(false);
            done();
        });
    });
});

describe("Teammatch Parsing", function () {

    it("should parse a Bundesliga Match", function (done) {
        fs.readFile('./test/turnierde/teammatch.html', 'utf8', function (err, fileContents) {
            var matches = scrapper.getMatchesFromTeammatch(fileContents);

            expect(matches.length).to.equal(7);
            expect(matches[2].league).to.equal("Bundesligen 2017/18");
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