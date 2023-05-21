package com.nonoka.nhentai.helper

val crawlerMap = mapOf(
    ClientType.Gallery to WebDataCrawler(),
    ClientType.Detail to WebDataCrawler(),
    ClientType.Recommendation to WebDataCrawler()
)