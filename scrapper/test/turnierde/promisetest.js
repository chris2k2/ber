function sayHello(callback) {
    for (var i = 0; i < 10; i++) {
        callback(i);
    }
}

function listify(callee) {
    return new Promise(function (resolve, reject) {
        let list = [];
        let aggregatorFunction = function (arg) {
            list.push(arg);
        };

        callee(aggregatorFunction);

        resolve(list);
    });
}

listify(sayHello).then(x => console.log(x));