package com.example.data

import com.example.data.model.Crypto
import com.example.data.model.MarketIndex
import com.example.data.model.NewsArticle
import com.example.data.model.NotificationModel
import com.example.data.model.Stock

object SampleData {

    val marketIndices = listOf(
        MarketIndex("S&P 500", "S&P 500", "5,432.10", "+1.2%", true, "Index"),
        MarketIndex("NASDAQ", "NASDAQ", "17,120.50", "+0.8%", true, "Index"),
        MarketIndex("BTC", "Bitcoin", "$68,420", "+2.1%", true, "Crypto"),
        MarketIndex("ETH", "Ethereum", "$3,510", "+1.5%", true, "Crypto")
    )

    val stocks = listOf(
        Stock(
            symbol = "AAPL",
            name = "Apple Inc.",
            exchange = "NASDAQ",
            price = 189.43,
            change = 2.45,
            percentChange = 1.31,
            isPositive = true,
            marketCap = "2.95T",
            peRatio = 31.42,
            volume = "45.2M",
            avgVolume = "52.8M",
            high52w = "$199.62",
            low52w = "$124.17",
            divYield = "0.51%",
            beta = 1.28,
            category = "Technology",
            logoUrl = "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(200f, 180f, 250f, 220f, 190f, 150f, 160f, 170f, 100f, 120f, 80f)
        ),
        Stock(
            symbol = "TSLA",
            name = "Tesla Inc.",
            exchange = "NASDAQ",
            price = 175.22,
            change = -4.31,
            percentChange = -2.40,
            isPositive = false,
            marketCap = "558.1B",
            peRatio = 42.15,
            volume = "68.4M",
            avgVolume = "75.2M",
            high52w = "$271.00",
            low52w = "$138.80",
            divYield = "0.00%",
            beta = 2.41,
            category = "Automotive",
            logoUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(190f, 210f, 180f, 175f, 160f, 175f)
        ),
        Stock(
            symbol = "NVDA",
            name = "NVIDIA Corp",
            exchange = "NASDAQ",
            price = 1204.50,
            change = 39.60,
            percentChange = 3.40,
            isPositive = true,
            marketCap = "2.96T",
            peRatio = 72.80,
            volume = "51.1M",
            avgVolume = "48.9M",
            high52w = "$1255.00",
            low52w = "$408.20",
            divYield = "0.04%",
            beta = 1.68,
            category = "Semiconductors",
            logoUrl = "https://images.unsplash.com/photo-1591488320449-011701bb6704?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(100f, 120f, 150f, 220f, 280f, 320f)
        ),
        Stock(
            symbol = "MSFT",
            name = "Microsoft Corp",
            exchange = "NASDAQ",
            price = 448.90,
            change = 5.20,
            percentChange = 1.17,
            isPositive = true,
            marketCap = "3.33T",
            peRatio = 38.20,
            volume = "21.4M",
            avgVolume = "24.1M",
            high52w = "$450.30",
            low52w = "$309.45",
            divYield = "0.67%",
            beta = 0.89,
            category = "Technology",
            logoUrl = "https://images.unsplash.com/photo-1633419461186-7d40a38105ec?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(350f, 380f, 400f, 420f, 448f)
        ),
        Stock(
            symbol = "AMZN",
            name = "Amazon.com Inc.",
            exchange = "NASDAQ",
            price = 186.20,
            change = 1.80,
            percentChange = 0.98,
            isPositive = true,
            marketCap = "1.94T",
            peRatio = 51.40,
            volume = "33.8M",
            avgVolume = "41.2M",
            high52w = "$191.70",
            low52w = "$126.30",
            divYield = "0.00%",
            beta = 1.15,
            category = "E-Commerce / Cloud",
            logoUrl = "https://images.unsplash.com/photo-1523474253046-8cd2748b5fd2?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(140f, 155f, 170f, 186f)
        )
    )

    val cryptos = listOf(
        Crypto(
            id = "bitcoin",
            name = "Bitcoin",
            symbol = "BTC",
            price = 68420.0,
            change24h = 1410.0,
            percentChange24h = 2.10,
            isPositive = true,
            marketCap = "$1.35T",
            volume24h = "$28.4B",
            logoUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(62000f, 64000f, 63500f, 66000f, 68420f)
        ),
        Crypto(
            id = "ethereum",
            name = "Ethereum",
            symbol = "ETH",
            price = 3510.0,
            change24h = 51.8,
            percentChange24h = 1.50,
            isPositive = true,
            marketCap = "$422B",
            volume24h = "$14.2B",
            logoUrl = "https://images.unsplash.com/photo-1622979135225-d2ba269bc1bd?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(3200f, 3350f, 3400f, 3510f)
        ),
        Crypto(
            id = "solana",
            name = "Solana",
            symbol = "SOL",
            price = 152.40,
            change24h = 6.20,
            percentChange24h = 4.24,
            isPositive = true,
            marketCap = "$70.8B",
            volume24h = "$3.1B",
            logoUrl = "https://images.unsplash.com/photo-1639762681485-074b7f938ba0?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(130f, 142f, 139f, 152f)
        ),
        Crypto(
            id = "ripple",
            name = "XRP",
            symbol = "XRP",
            price = 0.528,
            change24h = -0.012,
            percentChange24h = -2.22,
            isPositive = false,
            marketCap = "$29.4B",
            volume24h = "$1.05B",
            logoUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?auto=format&fit=crop&q=80&w=200",
            historyPoints = listOf(0.55f, 0.54f, 0.53f, 0.528f)
        )
    )

    val articles = listOf(
        NewsArticle(
            id = "art-1",
            title = "Global Markets Surge Amid Tech Growth",
            category = "MARKETS",
            source = "Financial Times",
            publishedAgo = "2m ago",
            views = "18.5k",
            author = "Financial Times",
            imageUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
            isBreaking = true,
            summaryPoints = listOf(
                "Major central banks simultaneously announced a pause in interest rate hikes, citing stabilizing inflation metrics across key global economies.",
                "Equities surged in response, with the S&P 500 gaining 2.4% and European indices seeing similar rallies led by tech and cyclical sectors.",
                "Bond yields retreated sharply, easing financing conditions for highly leveraged firms and providing a tailwind for growth stocks."
            ),
            fullContent = listOf(
                "In a coordinated communication effort rarely seen since the global financial crisis, central bank governors from the Federal Reserve, the European Central Bank, and the Bank of England signaled today that the aggressive tightening cycle of the past two years has likely reached its zenith.",
                "The announcement triggered an immediate and forceful rally across global asset classes. Equities, previously weighed down by the prospect of 'higher for longer' rates, saw significant inflows. Tech megacaps, highly sensitive to discount rates, led the charge, pulling broader indices upward in a sustained morning session rally."
            ),
            whyItMatters = "This synchronized pause removes a massive overhang of uncertainty from financial markets. For institutional investors, it shifts the focus from managing duration risk back to fundamental earnings growth.",
            quote = "\"We are now observing the delayed effects of our monetary policy working their way through the broader economy. Given the current trajectory of disinflation, further tightening appears unwarranted at this juncture.\"",
            relatedSymbols = listOf("AAPL", "NVDA", "MSFT")
        ),
        NewsArticle(
            id = "art-2",
            title = "Apple unveils new AI features for upcoming iPhone lineup, sending stock higher.",
            category = "TECHNOLOGY",
            source = "TechCrunch",
            publishedAgo = "2h ago",
            views = "24.1k",
            author = "Elena Rostova",
            imageUrl = "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&q=80&w=200",
            isBreaking = false,
            summaryPoints = listOf(
                "Apple showcased new generative AI capabilities integrated directly into iOS system applications.",
                "Wall Street analysts reacted positively, raising price targets for AAPL to an average of $220.",
                "Supply chain checks indicate heightened component orders ahead of the upcoming product refresh."
            ),
            fullContent = listOf(
                "Apple Inc. held a keynote presentation introducing its updated artificial intelligence suite embedded into upcoming operating systems.",
                "Demonstrations highlighted context-aware Siri routines, automatic summary generators in Mail and Notes, and privacy-first on-device execution paired with secure cloud computing."
            ),
            whyItMatters = "AI features could kickstart a major multi-year hardware upgrade cycle among iPhone owners holding older devices.",
            quote = "\"On-device intelligence with strict user privacy guarantees represents the next decade of personal computing.\"",
            relatedSymbols = listOf("AAPL")
        ),
        NewsArticle(
            id = "art-3",
            title = "Federal Reserve Holds Interest Rates Steady, Signals Future Cuts",
            category = "ECONOMY",
            source = "Bloomberg",
            publishedAgo = "5m ago",
            views = "14.2k",
            author = "Elena Rostova",
            imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&q=80&w=200",
            summaryPoints = listOf(
                "Federal Open Market Committee maintained the benchmark federal funds rate range at 5.25%-5.50%.",
                "Updated dot plot projections indicate up to three rate reductions anticipated over the coming 12 months.",
                "Inflation indicators show steady progress toward the Federal Reserve's long-term 2% target."
            ),
            fullContent = listOf(
                "Recent macroeconomic data has provided policymakers with necessary breathing room.",
                "Core inflation prints across the US and Eurozone have decelerated faster than anticipated in the last quarter."
            ),
            whyItMatters = "Lower interest rates reduce borrowing costs across consumer mortgage products, corporate debt refinancing, and business investment.",
            quote = "\"We will proceed carefully based on incoming economic data rather than fixed calendar dates.\"",
            relatedSymbols = listOf("S&P 500", "NASDAQ")
        ),
        NewsArticle(
            id = "art-4",
            title = "New Advances in AI Chip Manufacturing Promise 10x Efficiency",
            category = "TECHNOLOGY",
            source = "TechCrunch",
            publishedAgo = "15m ago",
            views = "9.8k",
            author = "Marcus Vance",
            imageUrl = "https://images.unsplash.com/photo-1591488320449-011701bb6704?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
            summaryPoints = listOf(
                "Next-gen semiconductor lithography enables dramatic energy reduction for AI data center clusters.",
                "Key semiconductor suppliers report initial test chip yields exceeding baseline performance targets."
            ),
            fullContent = listOf(
                "A breakthrough in extreme ultraviolet (EUV) patterning allows transistor density improvements without thermal degradation.",
                "Major tech infrastructure buyers are prioritizing energy-efficient server racks to meet sustainability targets."
            ),
            whyItMatters = "Power availability is currently the biggest bottleneck holding back massive AI data center expansions worldwide.",
            quote = "\"Energy efficiency is now as important as raw computing power in modern processor architecture.\"",
            relatedSymbols = listOf("NVDA", "TSM")
        ),
        NewsArticle(
            id = "art-5",
            title = "Supply Chain Disruptions Ease Quicker Than Expected, Analysts Say",
            category = "GLOBAL TRADE",
            source = "Reuters",
            publishedAgo = "1h ago",
            views = "11.1k",
            author = "Sarah Chen",
            imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
            summaryPoints = listOf(
                "Global shipping container freight rates have normalized toward historical pre-pandemic levels.",
                "Port congestion metrics at primary global logistics hubs show significant operational clearing."
            ),
            fullContent = listOf(
                "Logistics managers report smoother maritime transit times and stabilized air cargo rates.",
                "Improved inventory management systems have reduced reliance on emergency expedited shipping."
            ),
            whyItMatters = "Easing supply chain bottlenecks directly suppresses cost-push inflation for consumer electronics and retail goods.",
            relatedSymbols = listOf("AMZN")
        ),
        NewsArticle(
            id = "art-6",
            title = "Wildlife Conservation Act Passes Senate, Protecting Endangered Habitats",
            category = "ANIMALS",
            source = "National Geographic",
            publishedAgo = "3h ago",
            views = "15.4k",
            author = "Dr. Robert Thorne",
            imageUrl = "https://images.unsplash.com/photo-1534567153574-2b12153a87f0?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
            summaryPoints = listOf(
                "Bipartisan legislation secures multi-billion-dollar funding for endangered species corridors.",
                "New protections established for marine life sanctuaries and coastal wetlands."
            ),
            fullContent = listOf(
                "Environmental scientists celebrated a landmark legislative victory today as the Wildlife Conservation Act was signed into law.",
                "The bill allocates crucial resources to prevent habitat fragmentation and combat illegal wildlife trafficking globally."
            ),
            whyItMatters = "Preserving biodiversity is essential for maintaining resilient ecological food webs and natural carbon sequestration.",
            relatedSymbols = emptyList()
        ),
        NewsArticle(
            id = "art-7",
            title = "Rare Bald Eagle Nesting Site Discovered in Protected Wetland Reserve",
            category = "BIRDS",
            source = "Audubon Society",
            publishedAgo = "4h ago",
            views = "21.0k",
            author = "Claire Dubois",
            imageUrl = "https://images.unsplash.com/photo-1611689342806-0863700ce1e4?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
            isBreaking = true,
            summaryPoints = listOf(
                "Ornithologists confirm active nesting pair of bald eagles in urban wetland reserve.",
                "Live webcam streaming set up to monitor nesting and chick rearing behavior."
            ),
            fullContent = listOf(
                "Wildlife rangers monitoring local wetlands spotted a pair of majestic bald eagles building a massive nesting structure.",
                "Conservation experts note this marks a remarkable recovery for raptor populations in the region."
            ),
            whyItMatters = "Successful urban nesting demonstrates that protected natural reserves can thrive alongside metropolitan growth.",
            relatedSymbols = emptyList()
        ),
        NewsArticle(
            id = "art-8",
            title = "Annual Bird Migration Patterns Reveal Fascinating Adaptation to Climate Shift",
            category = "BIRDS",
            source = "Scientific American",
            publishedAgo = "6h ago",
            views = "12.8k",
            author = "Dr. Alan Grant",
            imageUrl = "https://images.unsplash.com/photo-14444653614773-995cb1ef9efa?auto=format&fit=crop&q=80&w=800",
            authorImageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200",
            summaryPoints = listOf(
                "Avian tracking studies show species adjusting migration timing by up to two weeks.",
                "Radar ornithology maps unprecedented long-distance navigation routes across continents."
            ),
            fullContent = listOf(
                "Researchers analyzing satellite telemetry data from migratory songbirds and raptors have published new findings on ecological adaptation.",
                "Birds are demonstrating remarkable behavioral plasticity in response to shifting seasonal temperatures."
            ),
            whyItMatters = "Understanding migration dynamics helps conservationists protect critical stopover habitats along major flyways.",
            relatedSymbols = emptyList()
        )
    )

    val notifications = listOf(
        NotificationModel(
            id = "notif-1",
            title = "Breaking News Alert",
            description = "Global Markets Surge Amid Tech Growth. S&P 500 up 2.4%.",
            category = "MARKETS",
            timestamp = "2m ago",
            isRead = false,
            targetType = "ARTICLE",
            targetId = "art-1"
        ),
        NotificationModel(
            id = "notif-2",
            title = "AAPL Price Movement",
            description = "Apple Inc. (AAPL) is up +1.31% today to $189.43.",
            category = "STOCK",
            timestamp = "1h ago",
            isRead = false,
            targetType = "STOCK",
            targetId = "AAPL"
        ),
        NotificationModel(
            id = "notif-3",
            title = "Crypto Market Update",
            description = "Bitcoin (BTC) broke past $68,400 with a 2.1% daily gain.",
            category = "CRYPTO",
            timestamp = "3h ago",
            isRead = true,
            targetType = "CRYPTO",
            targetId = "bitcoin"
        ),
        NotificationModel(
            id = "notif-4",
            title = "Fed Interest Rate Decision",
            description = "Federal Reserve holds interest rates steady and signals potential future cuts.",
            category = "ECONOMY",
            timestamp = "5h ago",
            isRead = true,
            targetType = "ARTICLE",
            targetId = "art-3"
        )
    )
}
