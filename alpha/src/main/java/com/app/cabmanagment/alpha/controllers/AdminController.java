package com.app.cabmanagment.alpha.controllers;

import com.app.cabmanagment.alpha.Repository.SourceRepository;
import com.app.cabmanagment.alpha.models.Vehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class AdminController {

    private static final Logger LOGGER = LogManager.getLogger(AdminController.class);
    private Vehicle veh_repository = new Vehicle();
    private SourceRepository sourceRepository = new SourceRepository();

    public void AdminController(Vehicle veh_repository) {
        this.veh_repository = veh_repository;
    }

    @RequestMapping(value = "/getallvehicles", method = RequestMethod.GET)
    ResponseEntity<Object> getall() {
        Object model = sourceRepository.getallData();
        return ResponseEntity.status(HttpStatus.OK).body(model.toString());
    }

    @RequestMapping(value = "/registerVehicle", method = RequestMethod.POST)
    ResponseEntity<Object> RegisterVehicle(Vehicle input_vehicle) {

        Object model = sourceRepository.registerVehicle(
                input_vehicle.getRegistration_no(),
                input_vehicle.getDriver_name(),
                input_vehicle.getContact(),
                input_vehicle.getCity()
        );
        return ResponseEntity.status(HttpStatus.OK).body(model.toString());
    }

    @RequestMapping(value = "/changeCity", method = RequestMethod.POST)
    ResponseEntity<Object> ChangeVehicleCity(Vehicle input_vehicle) {

        Object model = sourceRepository.changeVehCity(
                input_vehicle.getRegistration_no(),
                input_vehicle.getCity()
        );
        return ResponseEntity.status(HttpStatus.OK).body(model.toString());
    }
}
