package uk.gov.hmcts.reform.judicialbooking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.judicialbooking.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.judicialbooking.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.judicialbooking.controller.advice.exception.ResourceNotFoundException;

@RestController
public class WelcomeController {

    @GetMapping(value = "/swagger")
    public String index() {
        return "redirect:swagger-ui.html";
    }

    @GetMapping("/exception/{type}")
    public ResponseEntity<String> getException(@PathVariable String type) {
        if (type.equals("invalidRequest")) {
            throw new InvalidRequest("Invalid Request");
        } else if (type.equals("resourceNotFoundException")) {
            throw new ResourceNotFoundException("Resource Not Found Exception");
        } else if (type.equals("httpMessageConversionException")) {
            throw new HttpMessageConversionException("Http Message Conversion Exception");
        } else if (type.equals("badRequestException")) {
            throw new BadRequestException("Bad Request Exception");
        }

        return null;
    }

    //This method is reserved for ASB topic testing. Need to be removed later.
    @PostMapping(value = "/send")
    public ResponseEntity<String> send(@RequestBody String body) {
        //log.info("Sending message for event");
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        return "welcome to Judicial Booking service";
    }
}
