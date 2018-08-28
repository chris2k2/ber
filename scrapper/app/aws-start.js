var express = require('express');
var bodyParser = require('body-parser');
var start = require('./service-start.js')
var AWS = require('aws-sdk');
var Consumer = require('sqs-consumer');
var dateFormat = require('dateformat');

AWS.config.update({region: 'eu-central-1'});
var sqs = new AWS.SQS({apiVersion: '2012-11-05'});

require('events').EventEmitter.prototype._maxListeners = 64;

const crawl = Consumer.create({
    queueUrl: "https://sqs.eu-central-1.amazonaws.com/261784908038/Crawl",
    handleMessage: (message, done) => {
        console.log(message);
        let msgObject = JSON.parse(message.Body);
        let endDate = msgObject.niceLastEndDate;
        let startDate = new Date(endDate);

        if (msgObject.source == "turnierde") {
            start.crawler(startDate.getFullYear(), startDate.getMonth(), "tournament").then(x => console.log("CRAWLER: " + x));
            start.crawler(startDate.getFullYear(), startDate.getMonth(), "league").then(x => console.log("CRAWLER: " + x));
        } else if (msgObject.source == "nuliga-bbv") {
            start.nuliga_crawler(startDate).then(x => console.log("CRAWLER: " + x));
        }

        done();
    }
});

const scrap = Consumer.create({
    queueUrl: "https://sqs.eu-central-1.amazonaws.com/261784908038/Scrap",
    handleMessage: (message, done) => {
        console.log(message);
        let tournaments = JSON.parse(message.Body);

        for (let tournament of tournaments) {
            if (tournament.source == "turnierde") {
                start.scrapper(tournament).then(x => console.log("SCRAPPER: " + x));
            } else if (tournament.source == "nuliga-bbv") {
                start.nuliga(tournament);
            }
        }
        done();
    }
});


crawl.on('error', (err) => {
    console.log(err.message);
});

scrap.on('error', (err) => {
    console.log(err.message);
});

crawl.start();
scrap.start();

