package com.example.fingerprint_backend.controller.biometrics.fingerprint;


import com.example.fingerprint_backend.controller.base.ModelController;
import com.example.fingerprint_backend.model.base.Model;
import com.example.fingerprint_backend.model.biometrics.fingerprint.FingerprintRecognitionModel;
import com.example.fingerprint_backend.repository.biometrics.fingerprint.FingerprintRecognitionModelRepository;
import com.example.fingerprint_backend.repository.biometrics.recognition.RecognitionRepository;
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
