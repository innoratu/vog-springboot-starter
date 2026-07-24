package com.vog.example.vog_tmf.tmf;

import java.util.List;

/** One page of a filtered collection plus the total matching count. */
public record PageWindow<T>(List<T> items, long total) {
}
