var expect = require("chai").expect;
var utils = require("../app/utils");

describe("League Mapping", function () {
    it("maps 1.Bundesliga to 0", function (done) {
        let id = utils.getLeagueDepthByName("1.Bundesliga");
        expect(id).to.equal(0);

        done();
    });

    it("maps 2.Bundesliga with glitter to 1", function (done) {
        let id = utils.getLeagueDepthByName("2.Bundesliga - asdf");
        expect(id).to.equal(1);

        done();
    });


    it("maps some random stuff with glitter to -1", function (done) {
        let id = utils.getLeagueDepthByName("U19 some random thing asdf");
        expect(id).to.equal(-1);

        done();
    });
});
