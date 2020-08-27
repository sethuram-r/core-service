package smarshare.coreservice.cache.model;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smarshare.coreservice.cache.DescendingScoreComparator;
import smarshare.coreservice.cache.FileSystemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Component
public class CacheManager {


    private final FileSystemManager fileSystemManager;
    private final DescendingScoreComparator descendingScoreComparator;
    private List<CacheEntry> cacheContainer;
    private int maxLimitOfCacheContainer = 10;


    @Autowired
    CacheManager(FileSystemManager fileSystemManager, DescendingScoreComparator descendingScoreComparator) {
        this.fileSystemManager = fileSystemManager;
        this.descendingScoreComparator = descendingScoreComparator;
        this.cacheContainer = new ArrayList<>() {
            @Override
            public boolean add(CacheEntry newCacheEntry) {

                boolean isCacheEntryExists = cacheContainer.stream()
                        .anyMatch( cacheContainerEntries -> cacheContainerEntries.getCacheInFileSystem().getFileName().equals( newCacheEntry.getCacheInFileSystem().getFileName() ) );
                if (!isCacheEntryExists) {
                    super.add( newCacheEntry );
                    cacheContainer.sort( descendingScoreComparator );
                    return true;
                }
                log.debug( "The Cache Entry Exists Already !!!" );
                return false;
            }
        };
    }

    public int getMaxLimitOfCacheContainer() {
        return maxLimitOfCacheContainer;
    }

    public void setMaxLimitOfCacheContainer(int maxLimitOfCacheContainer) {
        this.maxLimitOfCacheContainer = maxLimitOfCacheContainer;
    }

    private boolean didCacheContainerReachedThreshold() {
        return (this.cacheContainer.size() == this.maxLimitOfCacheContainer) ? Boolean.TRUE : Boolean.FALSE;
    }

    private Integer getMinimumValue(IntStream cacheContainerScores) {
        return cacheContainerScores.min().getAsInt();
    }

    private List<CacheEntry> getMinimumGroupedCacheContainer(IntStream cacheContainerScores, Map<Integer, List<CacheEntry>> groupedCacheContainer) {
        return groupedCacheContainer.get( getMinimumValue( cacheContainerScores ) );
    }

    private Boolean refreshCacheContainer(List<CacheEntry> minimumGroupedCacheContainer, List<CacheEntry> cacheContainer) {
        log.info( "Inside routine execution plan" );
        CacheEntry cacheEntryToBeRemoved = minimumGroupedCacheContainer.get( 0 );
        FileToBeCached correspondingFileOfCacheEntryToBeDeleted = cacheEntryToBeRemoved.getCacheInFileSystem();
        if (cacheContainer.remove( cacheEntryToBeRemoved )) {
            fileSystemManager.deleteFileInCache( correspondingFileOfCacheEntryToBeDeleted.getFileName() );
            return Boolean.TRUE;
        } else return Boolean.FALSE;
    }

    private Boolean cacheReplace() {

        Map<Integer, List<CacheEntry>> groupedCacheEntriesByScores = this.cacheContainer.stream().collect( Collectors.groupingBy( CacheEntry::getScore ) );
        List<CacheEntry> minimumGroupedCacheContainerByScore = getMinimumGroupedCacheContainer( this.cacheContainer.stream().mapToInt( CacheEntry::getScore ), groupedCacheEntriesByScores );

        if (minimumGroupedCacheContainerByScore.size() == 1 && minimumGroupedCacheContainerByScore.get( 0 ).getScore() == 0 && minimumGroupedCacheContainerByScore.get( 0 ).getTimeToGetIntoRefreshExecutionPlan() == 1) {
            log.info( " Inside special execution plan" );
            minimumGroupedCacheContainerByScore.get( 0 ).resetAvoidUsualPlanCycle();

            minimumGroupedCacheContainerByScore =
                    getMinimumGroupedCacheContainer(
                            this.cacheContainer.stream()
                                    .mapToInt( CacheEntry::getScore )
                                    .filter( value -> value != getMinimumValue( this.cacheContainer.stream().mapToInt( CacheEntry::getScore ) ) ), groupedCacheEntriesByScores );

        }

        return refreshCacheContainer( minimumGroupedCacheContainerByScore, this.cacheContainer );
    }

    private void refreshCache() {
        log.info( "Before Refreshing Cache Size is " + this.cacheContainer.size() );
        if (didCacheContainerReachedThreshold()) cacheReplace();
        log.info( "After Refreshing Cache Size is " + this.cacheContainer.size() );
    }

    public Boolean createNewCacheEntry(FileToBeCached fileToBeCached) {

        log.info( "Inside createNewCacheEntry" );

        refreshCache();
        boolean isCacheEntryDone = cacheContainer.add( new CacheEntry( fileToBeCached, 1, 1 ) );
        log.info( "IsCacheEntryDone ----------> " + isCacheEntryDone );
        return (isCacheEntryDone && fileSystemManager.createFileInCache( fileToBeCached )) ? Boolean.TRUE : Boolean.FALSE;
    }


    private List<CacheEntry> getCurrentStateOfContainer() {
        return new ArrayList<>( this.cacheContainer );
    }

    private boolean incrementScoreOfCacheEntry(FileToBeCached cachedFileToBeUpdated) {
        for (CacheEntry cacheEntry : this.cacheContainer) {
            if (cacheEntry.getCacheInFileSystem().getFileName().equals( cachedFileToBeUpdated.getFileName() )) {
                return cacheEntry.incrementScore();
            }
        }
        return Boolean.FALSE;
    }

    public Boolean updatingCacheEntry(FileToBeCached cachedFileToBeUpdated) {

        log.info( "Inside updatingCacheEntry" );

        refreshCache();

        // savepoint
        List<CacheEntry> cacheContainerSnapShotBeforeUpdate = getCurrentStateOfContainer();

        if (incrementScoreOfCacheEntry( cachedFileToBeUpdated )) {
            cacheContainer.sort( descendingScoreComparator );
            if (fileSystemManager.updateFileInCache( cachedFileToBeUpdated )) return true;
            this.cacheContainer = cacheContainerSnapShotBeforeUpdate;
        }
        return false;
    }

    public Boolean deletingCacheEntry(String cachedFileToBeDeleted) {

        log.info( "Inside deletingCacheEntry " );

        //savepoint
        List<CacheEntry> cacheContainerSnapShotBeforeDelete = getCurrentStateOfContainer();

        boolean removed = this.cacheContainer.removeIf( cacheEntry -> cacheEntry.getCacheInFileSystem().getFileName().equals( cachedFileToBeDeleted ) );
        if (removed) {
            boolean cacheRemoved = fileSystemManager.deleteFileInCache( cachedFileToBeDeleted );
            if (cacheRemoved) return true;
            this.cacheContainer = cacheContainerSnapShotBeforeDelete;
        }
        return false;
    }


    public FileToBeCached getCachedObject(String fileName) {

        log.info( "Inside getCachedObject" );

        FileToBeCached cachedFileToBeRetrieved = fileSystemManager.retrieveCachedFile( fileName );
        if (null != cachedFileToBeRetrieved) {
            incrementScoreOfCacheEntry( cachedFileToBeRetrieved );
            return cachedFileToBeRetrieved;
        }
        return null;
    }

    public Boolean checkWhetherObjectExistInCache(String fileName) {
        log.info( "Inside checkWhetherObjectExistInCache" );
        return this.cacheContainer.stream().anyMatch( cacheEntry -> cacheEntry.getCacheInFileSystem().getFileName().equals( fileName ) );
    }
}
