package de.cweyermann.ber.players.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnknownPlayerException extends RuntimeException {
    private static final long serialVersionUID = -2262711592748637914L;

    public UnknownPlayerException(String matchId) {
        super("Unknown player: " + matchId);
    }
}