package de.up42.services.featureservice.api;

import de.up42.services.featureservice.entity.Collection;
import de.up42.services.featureservice.entity.Feature;
import de.up42.services.featureservice.service.FeatureLookupService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/features")
@Validated
public class FeaturesApi {

  final FeatureLookupService lookupService;

  @GetMapping
  public ResponseEntity<FeaturesResponseDTO> features() {
    return ResponseEntity.ok()
            .body(FeaturesResponseDTO.from(lookupService.getAllFeatures()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeatureResponseDTO> feature(@PathVariable String id) {
    return ResponseEntity.ok()
            .body(FeatureResponseDTO.from(lookupService.getFeatureById(id)));
  }

  @GetMapping("/{id}/quicklook")
  public ResponseEntity<byte[]> image(@PathVariable String id) {
    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(from(lookupService.getFeatureById(id)));
  }


  @Value
  @Builder
  static class FeaturesResponseDTO {

    List<FeatureResponseDTO> features;

    public static FeaturesResponseDTO from(List<Collection> featuresCollection) {
      return FeaturesResponseDTO.builder()
              .features(featuresCollection.stream()
                      .flatMap(collection -> collection.getFeatures()
                              .stream()
                              .map(FeatureResponseDTO::from))
                      .collect(Collectors.toList()))
              .build();
    }

  }

  @Value
  @Builder
  static class FeatureResponseDTO {
    String id;
    BigInteger timestamp;
    BigInteger beginViewingDate;
    BigInteger endViewingDate;
    String missionName;

    public static FeatureResponseDTO from(Feature feature) {
      return FeatureResponseDTO.builder()
              .id(feature.getProperties().getId())
              .timestamp(feature.getProperties().getTimestamp())
              .beginViewingDate(feature.getProperties()
                      .getAcquisition()
                      .getBeginViewingDate())
              .endViewingDate(feature.getProperties()
                      .getAcquisition()
                      .getEndViewingDate())
              .missionName(feature.getProperties()
                      .getAcquisition()
                      .getMissionName())
              .build();
    }
  }

  private static byte[] from(Feature feature){
    String base64EncodedImage = feature.getProperties().getQuicklook();
    byte[] base64DecodedImage = Base64.decodeBase64(base64EncodedImage);
    return base64DecodedImage;
  }
}
