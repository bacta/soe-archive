package com.ocdsoft.bacta.soe.message

import com.ocdsoft.bacta.engine.conf.ini.IniBactaConfiguration
import com.ocdsoft.bacta.engine.network.client.ConnectionState
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection
import com.ocdsoft.bacta.soe.controller.*
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration
import com.ocdsoft.bacta.soe.router.SoeDevelopMessageRouter
import com.ocdsoft.bacta.soe.router.SwgMessageRouter
import com.ocdsoft.bacta.soe.util.SoeMessageUtil
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * Created by kburkhardt on 2/10/15.
 */
class MultiPacketSpec extends Specification {

    @Shared
    def soeMessageRouter
    
    @Shared
    List<ByteBuffer> processedPackets

    def setupSpec() {

        processedPackets = new ArrayList<ByteBuffer>()
        
        soeMessageRouter = new SoeDevelopMessageRouter(null, null)
        loadControllers(soeMessageRouter.metaClass.getProperty(soeMessageRouter, "controllers"))

    }

    def "CollectMessages"() {
        
        setup:
        def multiList = SoeMessageUtil.readTextPacketDump(new File(this.getClass().getResource("/multipackets.txt").getFile()))
        def bactaConfig = new IniBactaConfiguration()
        def networkConfig = new NetworkConfiguration(bactaConfig)
        
        def soeUdpConnection = new SoeUdpConnection(networkConfig, null, ConnectionState.DISCONNECTED, null)
        
        when:
        for(List<Byte> array : multiList) {
            ByteBuffer buffer = ByteBuffer.wrap(array.toArray(new byte[array.size()]))
            soeMessageRouter.routeMessage(soeUdpConnection, buffer)
        }
        
        then:
        noExceptionThrown()
        processedPackets.size() > 0
        for(ByteBuffer buffer : processedPackets) {
          println SoeMessageUtil.bytesToHex(buffer)
        }
    }
    
    
    def loadControllers(controllers) {

        def swgMessageRouter = Mock(SwgMessageRouter) {
            routeMessage(_,_,_,_) >> { byte zeroByte, int opcode, SoeUdpConnection connection, ByteBuffer buffer ->
                processedPackets.add(buffer)
            }
        }

        controllers.put(UdpPacketType.cUdpPacketConnect, Mock(SoeMessageController))
        controllers.put(UdpPacketType.cUdpPacketConfirm, Mock(SoeMessageController))
        controllers.put(UdpPacketType.cUdpPacketAckAll1, Mock(SoeMessageController))

        def multiController = new MultiController()
        multiController.setSoeMessageRouter(soeMessageRouter)
        multiController.setSwgMessageRouter(swgMessageRouter)

        controllers.put(UdpPacketType.cUdpPacketMulti, multiController)

        def reliableController = new ReliableMessageController()
        reliableController.setSoeMessageRouter(soeMessageRouter)
        reliableController.setSwgMessageRouter(swgMessageRouter)

        controllers.put(UdpPacketType.cUdpPacketReliable1, reliableController)

        def groupController = new GroupMessageController()
        groupController.setSoeMessageRouter(soeMessageRouter)
        groupController.setSwgMessageRouter(swgMessageRouter)

        controllers.put(UdpPacketType.cUdpPacketGroup, groupController)

        def zeroController = new ZeroEscapeController()
        zeroController.setSoeMessageRouter(soeMessageRouter)
        zeroController.setSwgMessageRouter(swgMessageRouter)

        controllers.put(UdpPacketType.cUdpPacketZeroEscape, zeroController)

        return controllers
    }
}