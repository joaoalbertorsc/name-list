package com.namelist.controller;

import com.namelist.model.Name;
import com.namelist.repository.NameRepository;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/names")
public class NameController {

    /** JSON body of the insert and update requests: {@code {"name": "..."}}. */
    public record NameRequest(String name) {
    }

    private final NameRepository nameRepository;

    public NameController(NameRepository nameRepository) {
        this.nameRepository = nameRepository;
    }

    /** Lists every stored name, oldest first, so the effect of the other operations can be checked. */
    @GetMapping
    public List<Name> listNames() {
        return nameRepository.findAll(Sort.by("id"));
    }

    @PostMapping
    public ResponseEntity<Name> insertName(@RequestBody NameRequest request) {
        Name saved = nameRepository.save(new Name(request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Renames every record called {@code name}, not only the first one found. */
    @PutMapping("/{name}")
    public ResponseEntity<Map<String, Integer>> updateName(@PathVariable String name, @RequestBody NameRequest request) {
        int updated = nameRepository.renameAll(name, request.name());
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    /** Deletes every record called {@code name}, not only the first one found. */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, Integer>> deleteName(@PathVariable String name) {
        int deleted = nameRepository.deleteAllByName(name);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }
}
