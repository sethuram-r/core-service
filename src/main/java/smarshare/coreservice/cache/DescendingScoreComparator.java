package smarshare.coreservice.cache;


import org.springframework.stereotype.Component;
import smarshare.coreservice.cache.model.CacheEntry;

import java.util.Comparator;

@Component
public class DescendingScoreComparator implements Comparator<CacheEntry> {
    @Override
    public int compare(CacheEntry entry1, CacheEntry entry2) {
        return entry2.getScore().compareTo( entry1.getScore() );
    }
}
