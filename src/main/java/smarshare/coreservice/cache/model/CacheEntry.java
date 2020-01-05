package smarshare.coreservice.cache.model;

public class CacheEntry {

    private FileToBeCached cacheInFileSystem;
    private Integer score;
    private Integer timeToGetIntoRefreshExecutionPlan;

    public CacheEntry(FileToBeCached cacheInFileSystem, Integer score, Integer timeToGetIntoRefreshExecutionPlan) {
        this.cacheInFileSystem = cacheInFileSystem;
        this.score = score;
        this.timeToGetIntoRefreshExecutionPlan = timeToGetIntoRefreshExecutionPlan;
    }

    public FileToBeCached getCacheInFileSystem() {
        return cacheInFileSystem;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getTimeToGetIntoRefreshExecutionPlan() {
        return timeToGetIntoRefreshExecutionPlan;
    }

    public Boolean incrementScore() {
        this.score = ++score;
        return Boolean.TRUE;
    }

    public void resetAvoidUsualPlanCycle() {
        this.timeToGetIntoRefreshExecutionPlan = 0;
    }
}
