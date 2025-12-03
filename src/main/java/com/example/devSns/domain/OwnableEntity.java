package com.example.devSns.domain;


public interface OwnableEntity {

    default boolean checkOwnership(Long memberId) {
        return getMember().getId().equals(memberId);
    }

    Member getMember();
}
