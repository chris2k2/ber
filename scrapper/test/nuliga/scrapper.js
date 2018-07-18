var expect = require("chai").expect;
var scrapper = require("../../app/nuliga/scrapper");
var fs = require("fs");

describe("Name Mapping", function () {
    it("should map simple names", function (done) {
        let id = scrapper.name2id("Chris Schmidt");
        expect(id).to.equal("07-SchmidtChris");
        done();
    });

    it("should map Umlaute", function (done) {
        let id = scrapper.name2id("Chris Müller");
        expect(id).to.equal("07-MüllerChris");
        done();
    });

    it("should ignore middle names", function() {
        let id = scrapper.name2id("Chris J. Müller");
        expect(id).to.equal("07-MüllerChris");
    });

    it("should handle single names", function() {
        let id = scrapper.name2id("Name");
        expect(id).to.equal("07-Name");
    })
});
