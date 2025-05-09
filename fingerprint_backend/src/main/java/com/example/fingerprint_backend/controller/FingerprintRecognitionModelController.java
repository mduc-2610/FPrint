package com.example.fingerprint_backend.controller;


import com.example.fingerprint_backend.model.FingerprintRecognitionModel;
import com.example.fingerprint_backend.repository.FingerprintRecognitionModelRepository;
import com.example.fingerprint_backend.repository.RecognitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fingerprint-recognition-model")
public class FingerprintRecognitionModelController
        extends ModelController<FingerprintRecognitionModel, String> {
    @Autowired
    private RecognitionRepository recognitionRepository;

    public FingerprintRecognitionModelController(FingerprintRecognitionModelRepository repository) {
        super(repository);
    }

}
