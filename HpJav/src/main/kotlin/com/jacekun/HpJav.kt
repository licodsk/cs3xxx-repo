package com.jacekun

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.app
import org.jsoup.Jsoup

class HpJav : MainAPI() {
    private val DEV = "DevDebug"
    private val globaltvType = TvType.Movie

    override var name = "HpJav TV"
    override var mainUrl = "https://hpjav.tv"
    override val supportedTypes = setOf(TvType.NSFW)
    override val hasDownloadSupport = false
    override val hasMainPage = true
    override val hasQuickSearch = false

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(mainUrl).document
        val all = ArrayList<HomePageList>()
        document.getElementsByTag("body")?.select("div.container")?.forEach { it2 ->
            // Fetch row title
            val title = it2.select("div.category-count > h3")?.text() ?: "<No Name Row>"
            // Fetch list of items and map
            val elements = it2.select("div.post-list > div")?.mapNotNull {
                //Fetch entries
                val aa = it.selectFirst("div.video-item > div > a") ?: return@mapNotNull null
                val linkTitle = it.select("div.entry-title > a") ?: return@mapNotNull null
                val link = fixUrlNull(linkTitle.attr("href")) ?: return@mapNotNull null
                val name = linkTitle.text() ?: "<No Title>"
                val image = if (link != "") { aa.select("img")?.attr("data-original") } else { null }
                val year = null
                //Log.i(DEV, "Result => (image) ${image} , (linkCode) ${linkCode}, (link) ${link}")

                MovieSearchResponse(
                    name,
                    link,
                    this.name,
                    globaltvType,
                    image,
                    year,
                    null,
                )
            }?.distinctBy { it.url } ?: listOf()
            if (elements.isNotEmpty()) {
                all.add(
                    HomePageList(
                        title, elements
                    )
                )
            }
        }
        return HomePageResponse(all.filter { hp -> hp.list.isNotEmpty() })
    }

    //TODO: Fix search
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document.getElementsByTag("body")
            .select("div.container > div > div.post-list")
            .select("div.col-md-3.col-sm-6.col-xs-6")
        //Log.i(DEV, "Result => $document")
        return document.map {
            val content = it.select("div.video-item > div > a").firstOrNull()
            //Log.i(DEV, "Result => $content")
            val linkCode = content?.attr("href") ?: ""
            val href = fixUrl(linkCode)
            val imgContent = content?.select("img")
            val title = imgContent?.attr("alt") ?: "<No Title Found>"
            val image = imgContent?.attr("data-original")?.trim('\'')
            val year = null
            //Log.i(DEV, "Result => Title: ${title}, Image: ${image}")

            MovieSearchResponse(
                title,
                href,
                this.name,
                globaltvType,
                image,
                year
            )
        }
    }
    //TODO: Fix load
    override suspend fun load(url: String): LoadResponse {
        val response = app.get(url).text
        val document = Jsoup.parse(response)
        //Log.i(DEV, "Url => ${url}")
        val body = document.getElementsByTag("body")
            .select("div.video-box-ather.container > div.container > div")
            .select("div > div > img")?.firstOrNull()
        //Log.i(DEV, "Result => ${body}")
        // Video details
        val poster = body?.attr("src")
        val title = body?.attr("alt") ?: "<No Title>"
        val descript = "<No Synopsis found>"
        val year = null

        // Video link
        val streamUrl = ""
        return MovieLoadResponse(
            name = title,
            url = url,
            apiName = this.name,
            type = globaltvType,
            dataUrl = streamUrl,
            posterUrl = poster,
            year = year,
            plot = descript,
            comingSoon = true
        )
    }
}