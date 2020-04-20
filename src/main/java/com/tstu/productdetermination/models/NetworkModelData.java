package com.tstu.productdetermination.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.datavec.api.split.InputSplit;

@Getter
@AllArgsConstructor
@Builder
public class NetworkModelData {
    private InputSplit trainData;
    private InputSplit testData;
}
