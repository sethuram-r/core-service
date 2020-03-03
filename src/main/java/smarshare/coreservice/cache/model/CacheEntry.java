package smarshare.coreservice.cache.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public @Data
class CacheEntry {

    private FileToBeCached cacheInFileSystem;
    private Integer score;
    private Integer timeToGetIntoRefreshExecutionPlan;


    public Boolean incrementScore() {
        this.score = ++score;
        return Boolean.TRUE;
    }

    public void resetAvoidUsualPlanCycle() {
        this.timeToGetIntoRefreshExecutionPlan = 0;
    }
}
