package io.springbatch.springbatch.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Member {

    private String id;

    @Builder
    public Member(String id) {
        this.id = id;
    }
}
