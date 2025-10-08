package com.spring.schoolmate.dto.eatphoto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EatPhotoReq {

  private Long studentId;
  private String eatimageUrl;
}