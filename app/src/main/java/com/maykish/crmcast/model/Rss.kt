package com.maykish.crmcast.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "item", strict = false)
data class RssItem(
    @field:Element(name = "title")
    var title: String? = null,

    @field:Element(name = "link")
    var link: String? = null,
)

@Root(name = "channel", strict = false)
data class RssChannel(
    @field:ElementList(inline = true, entry = "item")
    var items: List<RssItem>? = null
)

@Root(name = "rss", strict = false)
data class RssFeed(
    @field:Element(name = "channel")
    var channel: RssChannel? = null
)
