package org.pulsar.currency.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "code"})
public class Currency {

    private UUID id;
    private String code;
    private String fullName;
    private String sign;
}
