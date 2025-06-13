package com.fijimf.deepfij.ml;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Models {

    public List<String> getKeys(){
        return List.of("basic-margin");
    }
}
