/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.plugin.hive;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;
import io.airlift.configuration.DefunctConfig;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import io.airlift.units.MaxDataSize;
import io.airlift.units.MinDataSize;
import io.airlift.units.MinDuration;
import io.prestosql.orc.OrcWriteValidation.OrcWriteValidationMode;
import io.prestosql.plugin.hive.s3.S3FileSystemType;
import org.joda.time.DateTimeZone;

import javax.annotation.Nullable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static io.airlift.units.DataSize.Unit.MEGABYTE;
import static java.util.concurrent.TimeUnit.MINUTES;

@DefunctConfig({
        "dfs.domain-socket-path",
        "hive.file-system-cache-ttl",
        "hive.max-global-split-iterator-threads",
        "hive.max-sort-files-per-bucket",
        "hive.bucket-writing",
        "hive.optimized-reader.enabled",
        "hive.orc.optimized-writer.enabled",
        "hive.rcfile-optimized-writer.enabled",
})
public class HiveConfig
{
    private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private String timeZone = TimeZone.getDefault().getID();

    private DataSize maxSplitSize = new DataSize(64, MEGABYTE);
    private int maxPartitionsPerScan = 100_000;
    private int maxOutstandingSplits = 1_000;
    private DataSize maxOutstandingSplitsSize = new DataSize(256, MEGABYTE);
    private int maxSplitIteratorThreads = 1_000;
    private int minPartitionBatchSize = 10;
    private int maxPartitionBatchSize = 100;
    private int maxInitialSplits = 200;
    private int splitLoaderConcurrency = 4;
    private Integer maxSplitsPerSecond;
    private DataSize maxInitialSplitSize;
    private int domainCompactionThreshold = 100;
    private DataSize writerSortBufferSize = new DataSize(64, MEGABYTE);
    private boolean forceLocalScheduling;
    private boolean recursiveDirWalkerEnabled;

    private int maxConcurrentFileRenames = 20;

    private boolean allowCorruptWritesForTesting;

    private long perTransactionMetastoreCacheMaximumSize = 1000;
    private HostAndPort metastoreSocksProxy;
    private Duration metastoreTimeout = new Duration(10, TimeUnit.SECONDS);

    private Duration ipcPingInterval = new Duration(10, TimeUnit.SECONDS);
    private Duration dfsTimeout = new Duration(60, TimeUnit.SECONDS);
    private Duration dfsConnectTimeout = new Duration(500, TimeUnit.MILLISECONDS);
    private Duration dfsKeyProviderCacheTtl = new Duration(30, TimeUnit.MINUTES);
    private int dfsConnectMaxRetries = 5;
    private boolean verifyChecksum = true;
    private String domainSocketPath;

    private S3FileSystemType s3FileSystemType = S3FileSystemType.PRESTO;

    private HiveStorageFormat hiveStorageFormat = HiveStorageFormat.ORC;
    private HiveCompressionCodec hiveCompressionCodec = HiveCompressionCodec.GZIP;
    private boolean respectTableFormat = true;
    private boolean immutablePartitions;
    private boolean createEmptyBucketFiles = true;
    private int maxPartitionsPerWriter = 100;
    private int maxOpenSortFiles = 50;
    private int writeValidationThreads = 16;

    private List<String> resourceConfigFiles = ImmutableList.of();

    private DataSize textMaxLineLength = new DataSize(100, MEGABYTE);

    private boolean useParquetColumnNames;
    private boolean failOnCorruptedParquetStatistics = true;
    private DataSize parquetMaxReadBlockSize = new DataSize(16, MEGABYTE);

    private boolean assumeCanonicalPartitionKeys;

    private boolean useOrcColumnNames;
    private boolean orcBloomFiltersEnabled;
    private double orcDefaultBloomFilterFpp = 0.05;
    private DataSize orcMaxMergeDistance = new DataSize(1, MEGABYTE);
    private DataSize orcMaxBufferSize = new DataSize(8, MEGABYTE);
    private DataSize orcTinyStripeThreshold = new DataSize(8, MEGABYTE);
    private DataSize orcStreamBufferSize = new DataSize(8, MEGABYTE);
    private DataSize orcMaxReadBlockSize = new DataSize(16, MEGABYTE);
    private boolean orcLazyReadSmallRanges = true;
    private boolean orcWriteLegacyVersion;
    private double orcWriterValidationPercentage;
    private OrcWriteValidationMode orcWriterValidationMode = OrcWriteValidationMode.BOTH;

    private boolean rcfileWriterValidate;

    private HiveMetastoreAuthenticationType hiveMetastoreAuthenticationType = HiveMetastoreAuthenticationType.NONE;
    private HdfsAuthenticationType hdfsAuthenticationType = HdfsAuthenticationType.NONE;
    private boolean hdfsImpersonationEnabled;
    private boolean hdfsWireEncryptionEnabled;

    private boolean skipDeletionForAlter;
    private boolean skipTargetCleanupOnRollback;

    private boolean bucketExecutionEnabled = true;
    private boolean sortedWritingEnabled = true;

    private int fileSystemMaxCacheSize = 1000;

    private boolean optimizeMismatchedBucketCount;
    private boolean writesToNonManagedTablesEnabled;
    private boolean createsOfNonManagedTablesEnabled = true;

    private boolean tableStatisticsEnabled = true;
    private int partitionStatisticsSampleSize = 100;
    private boolean ignoreCorruptedStatistics;
    private boolean collectColumnStatisticsOnWrite = true;

    private String recordingPath;
    private boolean replay;
    private Duration recordingDuration = new Duration(10, MINUTES);
    private boolean s3SelectPushdownEnabled;
    private int s3SelectPushdownMaxConnections = 500;

    private boolean isTemporaryStagingDirectoryEnabled = true;
    private String temporaryStagingDirectoryPath = "/tmp/presto-${USER}";

    private Duration fileStatusCacheExpireAfterWrite = new Duration(1, TimeUnit.MINUTES);
    private long fileStatusCacheMaxSize = 1000 * 1000;
    private List<String> fileStatusCacheTables = ImmutableList.of();

    public int getMaxInitialSplits()
    {
        return maxInitialSplits;
    }

    @Config("hive.max-initial-splits")
    public HiveConfig setMaxInitialSplits(int maxInitialSplits)
    {
        this.maxInitialSplits = maxInitialSplits;
        return this;
    }

    public DataSize getMaxInitialSplitSize()
    {
        if (maxInitialSplitSize == null) {
            return new DataSize(maxSplitSize.getValue() / 2, maxSplitSize.getUnit());
        }
        return maxInitialSplitSize;
    }

    @Config("hive.max-initial-split-size")
    public HiveConfig setMaxInitialSplitSize(DataSize maxInitialSplitSize)
    {
        this.maxInitialSplitSize = maxInitialSplitSize;
        return this;
    }

    @Min(1)
    public int getSplitLoaderConcurrency()
    {
        return splitLoaderConcurrency;
    }

    @Config("hive.split-loader-concurrency")
    public HiveConfig setSplitLoaderConcurrency(int splitLoaderConcurrency)
    {
        this.splitLoaderConcurrency = splitLoaderConcurrency;
        return this;
    }

    @Min(1)
    @Nullable
    public Integer getMaxSplitsPerSecond()
    {
        return maxSplitsPerSecond;
    }

    @Config("hive.max-splits-per-second")
    @ConfigDescription("Throttles the maximum number of splits that can be assigned to tasks per second")
    public HiveConfig setMaxSplitsPerSecond(Integer maxSplitsPerSecond)
    {
        this.maxSplitsPerSecond = maxSplitsPerSecond;
        return this;
    }

    @Min(1)
    public int getDomainCompactionThreshold()
    {
        return domainCompactionThreshold;
    }

    @Config("hive.domain-compaction-threshold")
    @ConfigDescription("Maximum ranges to allow in a tuple domain without compacting it")
    public HiveConfig setDomainCompactionThreshold(int domainCompactionThreshold)
    {
        this.domainCompactionThreshold = domainCompactionThreshold;
        return this;
    }

    @MinDataSize("1MB")
    @MaxDataSize("1GB")
    public DataSize getWriterSortBufferSize()
    {
        return writerSortBufferSize;
    }

    @Config("hive.writer-sort-buffer-size")
    public HiveConfig setWriterSortBufferSize(DataSize writerSortBufferSize)
    {
        this.writerSortBufferSize = writerSortBufferSize;
        return this;
    }

    public boolean isForceLocalScheduling()
    {
        return forceLocalScheduling;
    }

    @Config("hive.force-local-scheduling")
    public HiveConfig setForceLocalScheduling(boolean forceLocalScheduling)
    {
        this.forceLocalScheduling = forceLocalScheduling;
        return this;
    }

    @Min(1)
    public int getMaxConcurrentFileRenames()
    {
        return maxConcurrentFileRenames;
    }

    @Config("hive.max-concurrent-file-renames")
    public HiveConfig setMaxConcurrentFileRenames(int maxConcurrentFileRenames)
    {
        this.maxConcurrentFileRenames = maxConcurrentFileRenames;
        return this;
    }

    @Config("hive.recursive-directories")
    public HiveConfig setRecursiveDirWalkerEnabled(boolean recursiveDirWalkerEnabled)
    {
        this.recursiveDirWalkerEnabled = recursiveDirWalkerEnabled;
        return this;
    }

    public boolean getRecursiveDirWalkerEnabled()
    {
        return recursiveDirWalkerEnabled;
    }

    public DateTimeZone getDateTimeZone()
    {
        return DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone));
    }

    @NotNull
    public String getTimeZone()
    {
        return timeZone;
    }

    @Config("hive.time-zone")
    public HiveConfig setTimeZone(String id)
    {
        this.timeZone = (id != null) ? id : TimeZone.getDefault().getID();
        return this;
    }

    @NotNull
    public DataSize getMaxSplitSize()
    {
        return maxSplitSize;
    }

    @Config("hive.max-split-size")
    public HiveConfig setMaxSplitSize(DataSize maxSplitSize)
    {
        this.maxSplitSize = maxSplitSize;
        return this;
    }

    @Min(1)
    public int getMaxPartitionsPerScan()
    {
        return maxPartitionsPerScan;
    }

    @Config("hive.max-partitions-per-scan")
    @ConfigDescription("Maximum allowed partitions for a single table scan")
    public HiveConfig setMaxPartitionsPerScan(int maxPartitionsPerScan)
    {
        this.maxPartitionsPerScan = maxPartitionsPerScan;
        return this;
    }

    @Min(1)
    public int getMaxOutstandingSplits()
    {
        return maxOutstandingSplits;
    }

    @Config("hive.max-outstanding-splits")
    @ConfigDescription("Target number of buffered splits for each table scan in a query, before the scheduler tries to pause itself")
    public HiveConfig setMaxOutstandingSplits(int maxOutstandingSplits)
    {
        this.maxOutstandingSplits = maxOutstandingSplits;
        return this;
    }

    @MinDataSize("1MB")
    public DataSize getMaxOutstandingSplitsSize()
    {
        return maxOutstandingSplitsSize;
    }

    @Config("hive.max-outstanding-splits-size")
    @ConfigDescription("Maximum amount of memory allowed for split buffering for each table scan in a query, before the query is failed")
    public HiveConfig setMaxOutstandingSplitsSize(DataSize maxOutstandingSplits)
    {
        this.maxOutstandingSplitsSize = maxOutstandingSplits;
        return this;
    }

    @Min(1)
    public int getMaxSplitIteratorThreads()
    {
        return maxSplitIteratorThreads;
    }

    @Config("hive.max-split-iterator-threads")
    public HiveConfig setMaxSplitIteratorThreads(int maxSplitIteratorThreads)
    {
        this.maxSplitIteratorThreads = maxSplitIteratorThreads;
        return this;
    }

    @Deprecated
    public boolean getAllowCorruptWritesForTesting()
    {
        return allowCorruptWritesForTesting;
    }

    @Deprecated
    @Config("hive.allow-corrupt-writes-for-testing")
    @ConfigDescription("Allow Hive connector to write data even when data will likely be corrupt")
    public HiveConfig setAllowCorruptWritesForTesting(boolean allowCorruptWritesForTesting)
    {
        this.allowCorruptWritesForTesting = allowCorruptWritesForTesting;
        return this;
    }

    public long getPerTransactionMetastoreCacheMaximumSize()
    {
        return perTransactionMetastoreCacheMaximumSize;
    }

    @Min(1)
    @Config("hive.per-transaction-metastore-cache-maximum-size")
    public HiveConfig setPerTransactionMetastoreCacheMaximumSize(long perTransactionMetastoreCacheMaximumSize)
    {
        this.perTransactionMetastoreCacheMaximumSize = perTransactionMetastoreCacheMaximumSize;
        return this;
    }

    public HostAndPort getMetastoreSocksProxy()
    {
        return metastoreSocksProxy;
    }

    @Config("hive.metastore.thrift.client.socks-proxy")
    public HiveConfig setMetastoreSocksProxy(HostAndPort metastoreSocksProxy)
    {
        this.metastoreSocksProxy = metastoreSocksProxy;
        return this;
    }

    @NotNull
    public Duration getMetastoreTimeout()
    {
        return metastoreTimeout;
    }

    @Config("hive.metastore-timeout")
    public HiveConfig setMetastoreTimeout(Duration metastoreTimeout)
    {
        this.metastoreTimeout = metastoreTimeout;
        return this;
    }

    @Min(1)
    public int getMinPartitionBatchSize()
    {
        return minPartitionBatchSize;
    }

    @Config("hive.metastore.partition-batch-size.min")
    public HiveConfig setMinPartitionBatchSize(int minPartitionBatchSize)
    {
        this.minPartitionBatchSize = minPartitionBatchSize;
        return this;
    }

    @Min(1)
    public int getMaxPartitionBatchSize()
    {
        return maxPartitionBatchSize;
    }

    @Config("hive.metastore.partition-batch-size.max")
    public HiveConfig setMaxPartitionBatchSize(int maxPartitionBatchSize)
    {
        this.maxPartitionBatchSize = maxPartitionBatchSize;
        return this;
    }

    @NotNull
    public List<String> getResourceConfigFiles()
    {
        return resourceConfigFiles;
    }

    @Config("hive.config.resources")
    public HiveConfig setResourceConfigFiles(String files)
    {
        this.resourceConfigFiles = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(files);
        return this;
    }

    public HiveConfig setResourceConfigFiles(List<String> files)
    {
        this.resourceConfigFiles = ImmutableList.copyOf(files);
        return this;
    }

    @NotNull
    @MinDuration("1ms")
    public Duration getIpcPingInterval()
    {
        return ipcPingInterval;
    }

    @Config("hive.dfs.ipc-ping-interval")
    public HiveConfig setIpcPingInterval(Duration pingInterval)
    {
        this.ipcPingInterval = pingInterval;
        return this;
    }

    @NotNull
    @MinDuration("1ms")
    public Duration getDfsTimeout()
    {
        return dfsTimeout;
    }

    @Config("hive.dfs-timeout")
    public HiveConfig setDfsTimeout(Duration dfsTimeout)
    {
        this.dfsTimeout = dfsTimeout;
        return this;
    }

    @NotNull
    @MinDuration("0ms")
    public Duration getDfsKeyProviderCacheTtl()
    {
        return dfsKeyProviderCacheTtl;
    }

    @Config("hive.dfs.key-provider.cache-ttl")
    public HiveConfig setDfsKeyProviderCacheTtl(Duration dfsClientKeyProviderCacheTtl)
    {
        this.dfsKeyProviderCacheTtl = dfsClientKeyProviderCacheTtl;
        return this;
    }

    @MinDuration("1ms")
    @NotNull
    public Duration getDfsConnectTimeout()
    {
        return dfsConnectTimeout;
    }

    @Config("hive.dfs.connect.timeout")
    public HiveConfig setDfsConnectTimeout(Duration dfsConnectTimeout)
    {
        this.dfsConnectTimeout = dfsConnectTimeout;
        return this;
    }

    @Min(0)
    public int getDfsConnectMaxRetries()
    {
        return dfsConnectMaxRetries;
    }

    @Config("hive.dfs.connect.max-retries")
    public HiveConfig setDfsConnectMaxRetries(int dfsConnectMaxRetries)
    {
        this.dfsConnectMaxRetries = dfsConnectMaxRetries;
        return this;
    }

    public HiveStorageFormat getHiveStorageFormat()
    {
        return hiveStorageFormat;
    }

    @Config("hive.storage-format")
    public HiveConfig setHiveStorageFormat(HiveStorageFormat hiveStorageFormat)
    {
        this.hiveStorageFormat = hiveStorageFormat;
        return this;
    }

    public HiveCompressionCodec getHiveCompressionCodec()
    {
        return hiveCompressionCodec;
    }

    @Config("hive.compression-codec")
    public HiveConfig setHiveCompressionCodec(HiveCompressionCodec hiveCompressionCodec)
    {
        this.hiveCompressionCodec = hiveCompressionCodec;
        return this;
    }

    public boolean isRespectTableFormat()
    {
        return respectTableFormat;
    }

    @Config("hive.respect-table-format")
    @ConfigDescription("Should new partitions be written using the existing table format or the default Presto format")
    public HiveConfig setRespectTableFormat(boolean respectTableFormat)
    {
        this.respectTableFormat = respectTableFormat;
        return this;
    }

    public boolean isImmutablePartitions()
    {
        return immutablePartitions;
    }

    @Config("hive.immutable-partitions")
    @ConfigDescription("Can new data be inserted into existing partitions or existing unpartitioned tables")
    public HiveConfig setImmutablePartitions(boolean immutablePartitions)
    {
        this.immutablePartitions = immutablePartitions;
        return this;
    }

    public boolean isCreateEmptyBucketFiles()
    {
        return createEmptyBucketFiles;
    }

    @Config("hive.create-empty-bucket-files")
    @ConfigDescription("Create empty files for buckets that have no data")
    public HiveConfig setCreateEmptyBucketFiles(boolean createEmptyBucketFiles)
    {
        this.createEmptyBucketFiles = createEmptyBucketFiles;
        return this;
    }

    @Min(1)
    public int getMaxPartitionsPerWriter()
    {
        return maxPartitionsPerWriter;
    }

    @Config("hive.max-partitions-per-writers")
    @ConfigDescription("Maximum number of partitions per writer")
    public HiveConfig setMaxPartitionsPerWriter(int maxPartitionsPerWriter)
    {
        this.maxPartitionsPerWriter = maxPartitionsPerWriter;
        return this;
    }

    @Min(2)
    @Max(1000)
    public int getMaxOpenSortFiles()
    {
        return maxOpenSortFiles;
    }

    @Config("hive.max-open-sort-files")
    @ConfigDescription("Maximum number of writer temporary files to read in one pass")
    public HiveConfig setMaxOpenSortFiles(int maxOpenSortFiles)
    {
        this.maxOpenSortFiles = maxOpenSortFiles;
        return this;
    }

    public int getWriteValidationThreads()
    {
        return writeValidationThreads;
    }

    @Config("hive.write-validation-threads")
    @ConfigDescription("Number of threads used for verifying data after a write")
    public HiveConfig setWriteValidationThreads(int writeValidationThreads)
    {
        this.writeValidationThreads = writeValidationThreads;
        return this;
    }

    public String getDomainSocketPath()
    {
        return domainSocketPath;
    }

    @Config("hive.dfs.domain-socket-path")
    public HiveConfig setDomainSocketPath(String domainSocketPath)
    {
        this.domainSocketPath = domainSocketPath;
        return this;
    }

    @NotNull
    public S3FileSystemType getS3FileSystemType()
    {
        return s3FileSystemType;
    }

    @Config("hive.s3-file-system-type")
    public HiveConfig setS3FileSystemType(S3FileSystemType s3FileSystemType)
    {
        this.s3FileSystemType = s3FileSystemType;
        return this;
    }

    public boolean isVerifyChecksum()
    {
        return verifyChecksum;
    }

    @Config("hive.dfs.verify-checksum")
    public HiveConfig setVerifyChecksum(boolean verifyChecksum)
    {
        this.verifyChecksum = verifyChecksum;
        return this;
    }

    public boolean isUseOrcColumnNames()
    {
        return useOrcColumnNames;
    }

    @Config("hive.orc.use-column-names")
    @ConfigDescription("Access ORC columns using names from the file")
    public HiveConfig setUseOrcColumnNames(boolean useOrcColumnNames)
    {
        this.useOrcColumnNames = useOrcColumnNames;
        return this;
    }

    @NotNull
    public DataSize getOrcMaxMergeDistance()
    {
        return orcMaxMergeDistance;
    }

    @Config("hive.orc.max-merge-distance")
    public HiveConfig setOrcMaxMergeDistance(DataSize orcMaxMergeDistance)
    {
        this.orcMaxMergeDistance = orcMaxMergeDistance;
        return this;
    }

    @NotNull
    public DataSize getOrcMaxBufferSize()
    {
        return orcMaxBufferSize;
    }

    @Config("hive.orc.max-buffer-size")
    public HiveConfig setOrcMaxBufferSize(DataSize orcMaxBufferSize)
    {
        this.orcMaxBufferSize = orcMaxBufferSize;
        return this;
    }

    @NotNull
    public DataSize getOrcStreamBufferSize()
    {
        return orcStreamBufferSize;
    }

    @Config("hive.orc.stream-buffer-size")
    public HiveConfig setOrcStreamBufferSize(DataSize orcStreamBufferSize)
    {
        this.orcStreamBufferSize = orcStreamBufferSize;
        return this;
    }

    @NotNull
    public DataSize getOrcTinyStripeThreshold()
    {
        return orcTinyStripeThreshold;
    }

    @Config("hive.orc.tiny-stripe-threshold")
    public HiveConfig setOrcTinyStripeThreshold(DataSize orcTinyStripeThreshold)
    {
        this.orcTinyStripeThreshold = orcTinyStripeThreshold;
        return this;
    }

    @NotNull
    public DataSize getOrcMaxReadBlockSize()
    {
        return orcMaxReadBlockSize;
    }

    @Config("hive.orc.max-read-block-size")
    public HiveConfig setOrcMaxReadBlockSize(DataSize orcMaxReadBlockSize)
    {
        this.orcMaxReadBlockSize = orcMaxReadBlockSize;
        return this;
    }

    @Deprecated
    public boolean isOrcLazyReadSmallRanges()
    {
        return orcLazyReadSmallRanges;
    }

    // TODO remove config option once efficacy is proven
    @Deprecated
    @Config("hive.orc.lazy-read-small-ranges")
    @ConfigDescription("ORC read small disk ranges lazily")
    public HiveConfig setOrcLazyReadSmallRanges(boolean orcLazyReadSmallRanges)
    {
        this.orcLazyReadSmallRanges = orcLazyReadSmallRanges;
        return this;
    }

    public boolean isOrcBloomFiltersEnabled()
    {
        return orcBloomFiltersEnabled;
    }

    @Config("hive.orc.bloom-filters.enabled")
    public HiveConfig setOrcBloomFiltersEnabled(boolean orcBloomFiltersEnabled)
    {
        this.orcBloomFiltersEnabled = orcBloomFiltersEnabled;
        return this;
    }

    public double getOrcDefaultBloomFilterFpp()
    {
        return orcDefaultBloomFilterFpp;
    }

    @Config("hive.orc.default-bloom-filter-fpp")
    @ConfigDescription("ORC Bloom filter false positive probability")
    public HiveConfig setOrcDefaultBloomFilterFpp(double orcDefaultBloomFilterFpp)
    {
        this.orcDefaultBloomFilterFpp = orcDefaultBloomFilterFpp;
        return this;
    }

    public boolean isOrcWriteLegacyVersion()
    {
        return orcWriteLegacyVersion;
    }

    @Config("hive.orc.writer.use-legacy-version-number")
    @ConfigDescription("Write ORC files with a version number that is readable by Hive 2.0.0 to 2.2.0")
    public HiveConfig setOrcWriteLegacyVersion(boolean orcWriteLegacyVersion)
    {
        this.orcWriteLegacyVersion = orcWriteLegacyVersion;
        return this;
    }

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    public double getOrcWriterValidationPercentage()
    {
        return orcWriterValidationPercentage;
    }

    @Config("hive.orc.writer.validation-percentage")
    @ConfigDescription("Percentage of ORC files to validate after write by re-reading the whole file")
    public HiveConfig setOrcWriterValidationPercentage(double orcWriterValidationPercentage)
    {
        this.orcWriterValidationPercentage = orcWriterValidationPercentage;
        return this;
    }

    @NotNull
    public OrcWriteValidationMode getOrcWriterValidationMode()
    {
        return orcWriterValidationMode;
    }

    @Config("hive.orc.writer.validation-mode")
    @ConfigDescription("Level of detail in ORC validation. Lower levels require more memory.")
    public HiveConfig setOrcWriterValidationMode(OrcWriteValidationMode orcWriterValidationMode)
    {
        this.orcWriterValidationMode = orcWriterValidationMode;
        return this;
    }

    public boolean isRcfileWriterValidate()
    {
        return rcfileWriterValidate;
    }

    @Config("hive.rcfile.writer.validate")
    @ConfigDescription("Validate RCFile after write by re-reading the whole file")
    public HiveConfig setRcfileWriterValidate(boolean rcfileWriterValidate)
    {
        this.rcfileWriterValidate = rcfileWriterValidate;
        return this;
    }

    public boolean isAssumeCanonicalPartitionKeys()
    {
        return assumeCanonicalPartitionKeys;
    }

    @Config("hive.assume-canonical-partition-keys")
    public HiveConfig setAssumeCanonicalPartitionKeys(boolean assumeCanonicalPartitionKeys)
    {
        this.assumeCanonicalPartitionKeys = assumeCanonicalPartitionKeys;
        return this;
    }

    @MinDataSize("1B")
    @MaxDataSize("1GB")
    @NotNull
    public DataSize getTextMaxLineLength()
    {
        return textMaxLineLength;
    }

    @Config("hive.text.max-line-length")
    @ConfigDescription("Maximum line length for text files")
    public HiveConfig setTextMaxLineLength(DataSize textMaxLineLength)
    {
        this.textMaxLineLength = textMaxLineLength;
        return this;
    }

    public boolean isUseParquetColumnNames()
    {
        return useParquetColumnNames;
    }

    @Config("hive.parquet.use-column-names")
    @ConfigDescription("Access Parquet columns using names from the file")
    public HiveConfig setUseParquetColumnNames(boolean useParquetColumnNames)
    {
        this.useParquetColumnNames = useParquetColumnNames;
        return this;
    }

    public boolean isFailOnCorruptedParquetStatistics()
    {
        return failOnCorruptedParquetStatistics;
    }

    @Config("hive.parquet.fail-on-corrupted-statistics")
    @ConfigDescription("Fail when scanning Parquet files with corrupted statistics")
    public HiveConfig setFailOnCorruptedParquetStatistics(boolean failOnCorruptedParquetStatistics)
    {
        this.failOnCorruptedParquetStatistics = failOnCorruptedParquetStatistics;
        return this;
    }

    @NotNull
    public DataSize getParquetMaxReadBlockSize()
    {
        return parquetMaxReadBlockSize;
    }

    @Config("hive.parquet.max-read-block-size")
    public HiveConfig setParquetMaxReadBlockSize(DataSize parquetMaxReadBlockSize)
    {
        this.parquetMaxReadBlockSize = parquetMaxReadBlockSize;
        return this;
    }

    public boolean isOptimizeMismatchedBucketCount()
    {
        return optimizeMismatchedBucketCount;
    }

    @Config("hive.optimize-mismatched-bucket-count")
    public HiveConfig setOptimizeMismatchedBucketCount(boolean optimizeMismatchedBucketCount)
    {
        this.optimizeMismatchedBucketCount = optimizeMismatchedBucketCount;
        return this;
    }

    public List<String> getFileStatusCacheTables()
    {
        return fileStatusCacheTables;
    }

    @Config("hive.file-status-cache-tables")
    public HiveConfig setFileStatusCacheTables(String fileStatusCacheTables)
    {
        this.fileStatusCacheTables = SPLITTER.splitToList(fileStatusCacheTables);
        return this;
    }

    public long getFileStatusCacheMaxSize()
    {
        return fileStatusCacheMaxSize;
    }

    @Config("hive.file-status-cache-size")
    public HiveConfig setFileStatusCacheMaxSize(long fileStatusCacheMaxSize)
    {
        this.fileStatusCacheMaxSize = fileStatusCacheMaxSize;
        return this;
    }

    public Duration getFileStatusCacheExpireAfterWrite()
    {
        return fileStatusCacheExpireAfterWrite;
    }

    @Config("hive.file-status-cache-expire-time")
    public HiveConfig setFileStatusCacheExpireAfterWrite(Duration fileStatusCacheExpireAfterWrite)
    {
        this.fileStatusCacheExpireAfterWrite = fileStatusCacheExpireAfterWrite;
        return this;
    }

    public enum HiveMetastoreAuthenticationType
    {
        NONE,
        KERBEROS
    }

    @NotNull
    public HiveMetastoreAuthenticationType getHiveMetastoreAuthenticationType()
    {
        return hiveMetastoreAuthenticationType;
    }

    @Config("hive.metastore.authentication.type")
    @ConfigDescription("Hive Metastore authentication type")
    public HiveConfig setHiveMetastoreAuthenticationType(HiveMetastoreAuthenticationType hiveMetastoreAuthenticationType)
    {
        this.hiveMetastoreAuthenticationType = hiveMetastoreAuthenticationType;
        return this;
    }

    public enum HdfsAuthenticationType
    {
        NONE,
        KERBEROS,
    }

    @NotNull
    public HdfsAuthenticationType getHdfsAuthenticationType()
    {
        return hdfsAuthenticationType;
    }

    @Config("hive.hdfs.authentication.type")
    @ConfigDescription("HDFS authentication type")
    public HiveConfig setHdfsAuthenticationType(HdfsAuthenticationType hdfsAuthenticationType)
    {
        this.hdfsAuthenticationType = hdfsAuthenticationType;
        return this;
    }

    public boolean isHdfsImpersonationEnabled()
    {
        return hdfsImpersonationEnabled;
    }

    @Config("hive.hdfs.impersonation.enabled")
    @ConfigDescription("Should Presto user be impersonated when communicating with HDFS")
    public HiveConfig setHdfsImpersonationEnabled(boolean hdfsImpersonationEnabled)
    {
        this.hdfsImpersonationEnabled = hdfsImpersonationEnabled;
        return this;
    }

    public boolean isHdfsWireEncryptionEnabled()
    {
        return hdfsWireEncryptionEnabled;
    }

    @Config("hive.hdfs.wire-encryption.enabled")
    @ConfigDescription("Should be turned on when HDFS wire encryption is enabled")
    public HiveConfig setHdfsWireEncryptionEnabled(boolean hdfsWireEncryptionEnabled)
    {
        this.hdfsWireEncryptionEnabled = hdfsWireEncryptionEnabled;
        return this;
    }

    public boolean isSkipDeletionForAlter()
    {
        return skipDeletionForAlter;
    }

    @Config("hive.skip-deletion-for-alter")
    @ConfigDescription("Skip deletion of old partition data when a partition is deleted and then inserted in the same transaction")
    public HiveConfig setSkipDeletionForAlter(boolean skipDeletionForAlter)
    {
        this.skipDeletionForAlter = skipDeletionForAlter;
        return this;
    }

    public boolean isSkipTargetCleanupOnRollback()
    {
        return skipTargetCleanupOnRollback;
    }

    @Config("hive.skip-target-cleanup-on-rollback")
    @ConfigDescription("Skip deletion of target directories when a metastore operation fails")
    public HiveConfig setSkipTargetCleanupOnRollback(boolean skipTargetCleanupOnRollback)
    {
        this.skipTargetCleanupOnRollback = skipTargetCleanupOnRollback;
        return this;
    }

    public boolean isBucketExecutionEnabled()
    {
        return bucketExecutionEnabled;
    }

    @Config("hive.bucket-execution")
    @ConfigDescription("Enable bucket-aware execution: only use a single worker per bucket")
    public HiveConfig setBucketExecutionEnabled(boolean bucketExecutionEnabled)
    {
        this.bucketExecutionEnabled = bucketExecutionEnabled;
        return this;
    }

    public boolean isSortedWritingEnabled()
    {
        return sortedWritingEnabled;
    }

    @Config("hive.sorted-writing")
    @ConfigDescription("Enable writing to bucketed sorted tables")
    public HiveConfig setSortedWritingEnabled(boolean sortedWritingEnabled)
    {
        this.sortedWritingEnabled = sortedWritingEnabled;
        return this;
    }

    public int getFileSystemMaxCacheSize()
    {
        return fileSystemMaxCacheSize;
    }

    @Config("hive.fs.cache.max-size")
    @ConfigDescription("Hadoop FileSystem cache size")
    public HiveConfig setFileSystemMaxCacheSize(int fileSystemMaxCacheSize)
    {
        this.fileSystemMaxCacheSize = fileSystemMaxCacheSize;
        return this;
    }

    @Config("hive.non-managed-table-writes-enabled")
    @ConfigDescription("Enable writes to non-managed (external) tables")
    public HiveConfig setWritesToNonManagedTablesEnabled(boolean writesToNonManagedTablesEnabled)
    {
        this.writesToNonManagedTablesEnabled = writesToNonManagedTablesEnabled;
        return this;
    }

    public boolean getWritesToNonManagedTablesEnabled()
    {
        return writesToNonManagedTablesEnabled;
    }

    @Config("hive.non-managed-table-creates-enabled")
    @ConfigDescription("Enable non-managed (external) table creates")
    public HiveConfig setCreatesOfNonManagedTablesEnabled(boolean createsOfNonManagedTablesEnabled)
    {
        this.createsOfNonManagedTablesEnabled = createsOfNonManagedTablesEnabled;
        return this;
    }

    public boolean getCreatesOfNonManagedTablesEnabled()
    {
        return createsOfNonManagedTablesEnabled;
    }

    @Config("hive.table-statistics-enabled")
    @ConfigDescription("Enable use of table statistics")
    public HiveConfig setTableStatisticsEnabled(boolean tableStatisticsEnabled)
    {
        this.tableStatisticsEnabled = tableStatisticsEnabled;
        return this;
    }

    public boolean isTableStatisticsEnabled()
    {
        return tableStatisticsEnabled;
    }

    @Min(1)
    public int getPartitionStatisticsSampleSize()
    {
        return partitionStatisticsSampleSize;
    }

    @Config("hive.partition-statistics-sample-size")
    @ConfigDescription("Maximum sample size of the partitions column statistics")
    public HiveConfig setPartitionStatisticsSampleSize(int partitionStatisticsSampleSize)
    {
        this.partitionStatisticsSampleSize = partitionStatisticsSampleSize;
        return this;
    }

    public boolean isIgnoreCorruptedStatistics()
    {
        return ignoreCorruptedStatistics;
    }

    @Config("hive.ignore-corrupted-statistics")
    @ConfigDescription("Ignore corrupted statistics rather than failing")
    public HiveConfig setIgnoreCorruptedStatistics(boolean ignoreCorruptedStatistics)
    {
        this.ignoreCorruptedStatistics = ignoreCorruptedStatistics;
        return this;
    }

    public boolean isCollectColumnStatisticsOnWrite()
    {
        return collectColumnStatisticsOnWrite;
    }

    @Config("hive.collect-column-statistics-on-write")
    @ConfigDescription("Enables automatic column level statistics collection on write")
    public HiveConfig setCollectColumnStatisticsOnWrite(boolean collectColumnStatisticsOnWrite)
    {
        this.collectColumnStatisticsOnWrite = collectColumnStatisticsOnWrite;
        return this;
    }

    @Config("hive.metastore-recording-path")
    public HiveConfig setRecordingPath(String recordingPath)
    {
        this.recordingPath = recordingPath;
        return this;
    }

    public String getRecordingPath()
    {
        return recordingPath;
    }

    @Config("hive.replay-metastore-recording")
    public HiveConfig setReplay(boolean replay)
    {
        this.replay = replay;
        return this;
    }

    public boolean isReplay()
    {
        return replay;
    }

    @Config("hive.metastore-recording-duration")
    public HiveConfig setRecordingDuration(Duration recordingDuration)
    {
        this.recordingDuration = recordingDuration;
        return this;
    }

    @NotNull
    public Duration getRecordingDuration()
    {
        return recordingDuration;
    }

    public boolean isS3SelectPushdownEnabled()
    {
        return s3SelectPushdownEnabled;
    }

    @Config("hive.s3select-pushdown.enabled")
    @ConfigDescription("Enable query pushdown to AWS S3 Select service")
    public HiveConfig setS3SelectPushdownEnabled(boolean s3SelectPushdownEnabled)
    {
        this.s3SelectPushdownEnabled = s3SelectPushdownEnabled;
        return this;
    }

    @Min(1)
    public int getS3SelectPushdownMaxConnections()
    {
        return s3SelectPushdownMaxConnections;
    }

    @Config("hive.s3select-pushdown.max-connections")
    public HiveConfig setS3SelectPushdownMaxConnections(int s3SelectPushdownMaxConnections)
    {
        this.s3SelectPushdownMaxConnections = s3SelectPushdownMaxConnections;
        return this;
    }

    @Config("hive.temporary-staging-directory-enabled")
    @ConfigDescription("Should use (if possible) temporary staging directory for write operations")
    public HiveConfig setTemporaryStagingDirectoryEnabled(boolean temporaryStagingDirectoryEnabled)
    {
        this.isTemporaryStagingDirectoryEnabled = temporaryStagingDirectoryEnabled;
        return this;
    }

    public boolean isTemporaryStagingDirectoryEnabled()
    {
        return isTemporaryStagingDirectoryEnabled;
    }

    @Config("hive.temporary-staging-directory-path")
    @ConfigDescription("Location of temporary staging directory for write operations. Use ${USER} placeholder to use different location for each user.")
    public HiveConfig setTemporaryStagingDirectoryPath(String temporaryStagingDirectoryPath)
    {
        this.temporaryStagingDirectoryPath = temporaryStagingDirectoryPath;
        return this;
    }

    @NotNull
    public String getTemporaryStagingDirectoryPath()
    {
        return temporaryStagingDirectoryPath;
    }
}
