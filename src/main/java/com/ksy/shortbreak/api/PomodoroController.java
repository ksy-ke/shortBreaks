package com.ksy.shortbreak.api;

import com.ksy.shortbreak.service.PomodoroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import static java.util.stream.Collectors.toList;

@RequestMapping("/pomodoro")
public @Controller class PomodoroController {
    private static final Logger LOG = LoggerFactory.getLogger(PomodoroController.class);

    private final PomodoroService service;

    public @Autowired PomodoroController(PomodoroService service) {
        this.service = service;
    }

    @RolesAllowed("USER")
    @PostMapping("/new")
    public String newPomodoro(Model model) {
        LOG.debug("Registering new pomodoro");
        var pomodoro = PomodoroDto.ofPomodoro(service.initialize());
        model.addAttribute("pomodoro", pomodoro);
        LOG.debug("New pomodoro = {}", pomodoro);
        return "new_pomodoro";
    }

    @RolesAllowed("USER")
    @PostMapping("/update")
    public void updatePomodoro(@Valid PomodoroDto dto, Model model) {
        LOG.debug("Updating pomodoro = {}", dto);
        var pomodoro = dto.toPomodoro();
        service.update(pomodoro);
        LOG.debug("Pomodoro updated");
    }

    @RolesAllowed("USER")
    @GetMapping({"/all"})
    public String userPomodoros(@RequestParam int page, @RequestParam int size, Model model) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = authentication.getName();
        LOG.debug("User pomodoros of = {}", user);

        var pomodoros = service.pomodorosOfUser(user, page, size)
                .stream()
                .map(PomodoroDto::ofPomodoro)
                .collect(toList());
        var totalPomodoros = service.countPomodorosOfUser(user);
        model.addAttribute("pomodoros", pomodoros);
        model.addAttribute("total", totalPomodoros);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        LOG.debug("Got {} pomodoros", pomodoros.size());
        return "user_pomodoros";
    }

    // todo: get pomidoro by ID
}
