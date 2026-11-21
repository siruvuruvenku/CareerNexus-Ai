package com.careernexus.provider;

import java.util.List;

public interface JobProvider {
    String name();
    boolean enabled();
    List<ProviderJob> search(SearchCriteria criteria);
}

