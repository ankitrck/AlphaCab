package com.app.cabmanagment.alpha.controllers;

import com.app.cabmanagment.alpha.Repository.SourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class UserController {

    private SourceRepository sourceRepository = new SourceRepository();

    @RequestMapping(value = "/getavailable", method = RequestMethod.POST)
    ResponseEntity<Object> getavailable(String city) {
        Object model = sourceRepository.getavailableVeh(city);
        return ResponseEntity.status(HttpStatus.OK).body(model.toString());
    }

    @RequestMapping(value = "/bookcab", method = RequestMethod.POST)
    ResponseEntity<Object> bookcab(String regnumber) {
        Object model = sourceRepository.bookavailcab(regnumber);
        return ResponseEntity.status(HttpStatus.OK).body(model.toString());
    }

    @RequestMapping(value = "/endtrip", method = RequestMethod.POST)
    ResponseEntity<Object> endtrip(String regnumber) {
        Object model = sourceRepository.endtrip(regnumber);
        return ResponseEntity.status(HttpStatus.OK).body(model.toString());
    }
}
