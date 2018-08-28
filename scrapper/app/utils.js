exports.name2id = name2id;
exports.sleep = sleep;
exports.shuffle = Shuffle;
exports.getLeagueDepthByName = getLeagueDepthByName;


function name2id(name) {
    let id = "07-";

    let names = name.split(" ");
    let last = names.length - 1;
    id = id + names[last];

    if (names.length > 1) {
        id = id + names[0];
    }

    return id;
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function Shuffle(o) {
    for (var j, x, i = o.length; i; j = parseInt(Math.random() * i), x = o[--i], o[i] = o[j], o[j] = x) ;
    return o;
};

function getLeagueDepthByName(name) {
    let depth = -1;

    depth = nameMapsToId("1.Bundesliga", 0, name, depth);
    depth = nameMapsToId("2.Bundesliga", 1, name, depth);
    depth = nameMapsToId("Regionalliga", 2, name, depth);
    depth = nameMapsToId("Bayernliga", 3, name, depth);
    depth = nameMapsToId("Bezirksoberliga", 4, name, depth);
    depth = nameMapsToId("Bezirksliga", 5, name, depth);
    depth = nameMapsToId("Bezirksklasse A", 6, name, depth);
    depth = nameMapsToId("Bezirksklasse B", 7, name, depth);

    return depth;
}

function nameMapsToId(name, number, input, current) {
    if (current < 0 && input.includes(name)) {
        current = number;
    }

    return current;
}