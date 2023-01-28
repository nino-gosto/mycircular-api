package cat.udl.eps.softarch.demo.controller;

import cat.udl.eps.softarch.demo.domain.Request;
import cat.udl.eps.softarch.demo.domain.User;
import cat.udl.eps.softarch.demo.exception.NotFoundException;
import cat.udl.eps.softarch.demo.exception.UnauthorizedException;
import cat.udl.eps.softarch.demo.repository.ProdRequestRepository;
import cat.udl.eps.softarch.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@BasePathAwareController
public class ProdRequestController {

    @Autowired
    private ProdRequestRepository prodRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/prodRequests")
    public @ResponseBody List<Request> getOtherUserOwnRequests(@RequestParam(value = "username", required = false) String username) {
//        System.out.println(username);
        //aqui ya checkeamos que no pueda ser anonymous
        User currentUser = getCurrentUser();
        //si estamos buscando al usuario actual
        if (username.equals(currentUser.getUsername())) {
            return prodRequestRepository.findByRequester(currentUser);
        }
        //buscamos a otro usuario
        Optional<User> users = userRepository.findById(username);
        //miramos que exista el otro usuario
        if (users.isEmpty()) {
            throw new NotFoundException();
        }
        User otherUser = users.get();
        return prodRequestRepository.findByRequester(otherUser);


    }

    @Transactional
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/prodRequests")
    public @ResponseBody ResponseEntity<String> deleteUserRequests(@RequestParam(value = "username", required = false) String username) {
        //aqui ya checkeamos que no pueda ser anonymous
        User currentUser = getCurrentUser();
        if (username.equals(currentUser.getUsername())) {
            prodRequestRepository.deleteByRequester(currentUser);
            return new ResponseEntity<>(username, HttpStatus.NO_CONTENT);
        }

        Optional<User> users = userRepository.findById(username);
        if (users.isEmpty()) {
            return new ResponseEntity<>(username, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(username, HttpStatus.FORBIDDEN);
    }


    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        if (username.equals("anonymous")) {
            throw new UnauthorizedException();
        }
        Optional<User> currentUser = userRepository.findById(username);
        System.out.println(username);
        return currentUser.orElseGet(User::new);
    }


}
