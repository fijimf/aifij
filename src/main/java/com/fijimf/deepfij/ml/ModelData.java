package com.fijimf.deepfij.ml;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ModelData(String key, String name, String description, LocalDateTime asOf, List<Map<String, Object>> features, List<Map<String, Object>> targets){
}
