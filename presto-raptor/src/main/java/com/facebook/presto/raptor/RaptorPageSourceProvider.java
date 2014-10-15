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
package com.facebook.presto.raptor;

import com.facebook.presto.raptor.storage.LocalStorageManager;
import com.facebook.presto.spi.ConnectorColumnHandle;
import com.facebook.presto.spi.ConnectorPageSource;
import com.facebook.presto.spi.ConnectorPageSourceProvider;
import com.facebook.presto.spi.ConnectorSplit;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import java.util.List;
import java.util.UUID;

import static com.facebook.presto.raptor.util.Types.checkType;
import static com.google.common.base.Preconditions.checkNotNull;

public class RaptorPageSourceProvider
        implements ConnectorPageSourceProvider
{
    private final LocalStorageManager storageManager;

    @Inject
    public RaptorPageSourceProvider(LocalStorageManager storageManager)
    {
        this.storageManager = checkNotNull(storageManager, "storageManager is null");
    }

    @Override
    public ConnectorPageSource createPageSource(ConnectorSplit split, List<ConnectorColumnHandle> columns)
    {
        RaptorSplit raptorSplit = checkType(split, RaptorSplit.class, "split");

        UUID shardUuid = raptorSplit.getShardUuid();
        long countColumnId = raptorSplit.getCountColumnHandle().getColumnId();
        List<Long> columnIds = FluentIterable.from(columns).transform(raptorColumnId()).toList();

        return storageManager.getPageSource(shardUuid, columnIds, countColumnId);
    }

    private static Function<ConnectorColumnHandle, Long> raptorColumnId()
    {
        return new Function<ConnectorColumnHandle, Long>()
        {
            @Override
            public Long apply(ConnectorColumnHandle handle)
            {
                return checkType(handle, RaptorColumnHandle.class, "columnHandle").getColumnId();
            }
        };
    }
}
