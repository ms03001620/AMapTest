package com.example.amaptest.marker

import org.junit.Assert
import org.junit.Test

class BaseMarkerDataTest {

    private val stationsList = JsonTestUtil.readStation("json_stations.json")

    @Test
    fun testDuplicateId() {
        val stationsList570 = JsonTestUtil.readStation("json_stations570.json")

        mutableSetOf<String>().apply {
            stationsList570.forEach {
                this.add(it.id!!)
            }
        }.let {
            Assert.assertEquals(570, it.size)
        }
    }

    @Test
    fun baseMarkerDataCollection() {
        val prevCluster = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(3, 4)
        )

        val target = JsonTestUtil.mock(
            stationsList.subList(3, 4)
        )

        Assert.assertEquals(2, prevCluster.size)
        prevCluster.remove(target.first())
        Assert.assertEquals(1, prevCluster.size)
    }

    @Test
    fun baseMarkerDataCollectionEquals() {
        val source = JsonTestUtil.mock(
            stationsList.subList(0, 2)
        )

        val source1 = JsonTestUtil.mock(
            stationsList.subList(0, 2)
        )

        Assert.assertEquals(source1, source)

        val target = JsonTestUtil.mock(
            stationsList.subList(0, 1)
        )

        val target1 = JsonTestUtil.mock(
            stationsList.subList(0, 1)
        )

        Assert.assertEquals(target1, target)
        Assert.assertNotEquals(target, source)
    }

    @Test
    fun baseMarkerDataCollectionNotEquals() {
        val source = JsonTestUtil.mock(
            stationsList.subList(0, 2)
        )
        val source1 = JsonTestUtil.mock(
            stationsList.subList(0, 3)
        )
        Assert.assertNotEquals(source, source1)
    }

    @Test
    fun markClusterCollectionEquals() {
        val source = JsonTestUtil.mock(
            stationsList.subList(0, 2)
        )

        Assert.assertEquals(
            source, JsonTestUtil.mock(
                listOf(
                    stationsList.subList(0, 1).first(),
                    stationsList.subList(1, 2).first()
                )
            )
        )

        // order
        Assert.assertEquals(
            source, JsonTestUtil.mock(
                listOf(
                    stationsList.subList(1, 2).first(),
                    stationsList.subList(0, 1).first()
                )
            )
        )
    }

    @Test
    fun markClusterCollectionNotEquals() {
        Assert.assertNotEquals(
            JsonTestUtil.mock(
                stationsList.subList(0, 2)
            ),
            JsonTestUtil.mock(
                stationsList.subList(1, 3)
            )
        )

        Assert.assertNotEquals(
            JsonTestUtil.mock(
                stationsList.subList(0, 2)
            ),
            JsonTestUtil.mock(
                listOf(
                    stationsList.subList(0, 1).first(),
                    stationsList.subList(2, 3).first()
                )
            )
        )
    }

    @Test
    fun baseMarkerDataCollectionRemove() {
        val prevCluster = JsonTestUtil.mock(
            stationsList.subList(0, 2),
            stationsList.subList(3, 4)
        )

        val target = JsonTestUtil.mock(
            stationsList.subList(3, 4)
        )


        prevCluster.removeAll(target)
        Assert.assertEquals(1, prevCluster.size)
        Assert.assertEquals(2, prevCluster.first().getSize())
    }
}