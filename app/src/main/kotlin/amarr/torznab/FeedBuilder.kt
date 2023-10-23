package amarr.torznab

import amarr.MagnetLink
import amarr.torznab.model.Feed
import amarr.torznab.model.Feed.Channel.Item
import io.ktor.util.logging.*
import jamule.AmuleClient
import jamule.response.SearchResultsResponse.SearchFile

class FeedBuilder(private val amuleClient: AmuleClient, private val log: Logger) {

    fun buildFeed(query: String, offset: Int, limit: Int): Feed {
        log.debug("Starting search for query: {}, offset: {}, limit: {}", query, offset, limit)
        return buildFeed(amuleClient.searchSync(query).getOrThrow().files, offset, limit)
    }

    private fun buildFeed(items: List<SearchFile>, offset: Int, limit: Int) = Feed(
        channel = Feed.Channel(
            response = Feed.Channel.Response(
                offset = offset,
                total = items.size
            ),
            item = items
                .drop(offset)
                .take(limit)
                .map { result ->
                    Item(
                        title = result.fileName,
                        enclosure = Item.Enclosure(
                            url = MagnetLink.forAmarr(result.hash, result.fileName, result.sizeFull).toString(),
                            length = result.sizeFull
                        ),
                        attributes = listOf(
                            Item.TorznabAttribute("category", "1"),
                            Item.TorznabAttribute("seeders", result.completeSourceCount.toString()),
                            Item.TorznabAttribute("peers", result.sourceCount.toString()),
                            Item.TorznabAttribute("size", result.sizeFull.toString())
                        )
                    )
                }
        )
    )

}