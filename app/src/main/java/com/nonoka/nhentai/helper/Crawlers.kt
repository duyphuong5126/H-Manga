package com.nonoka.nhentai.helper

val crawlerMap = mapOf(
    ClientType.Gallery to WebDataCrawler(10000),
    ClientType.Detail to WebDataCrawler(10000),
    ClientType.ByPassing to WebDataCrawler(15000),
    ClientType.Comment to WebDataCrawler(10000)
)