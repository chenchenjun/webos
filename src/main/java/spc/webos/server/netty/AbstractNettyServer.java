package spc.webos.server.netty;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import spc.webos.service.seq.UUID;
import spc.webos.service.seq.impl.TimeMillisUUID;

public abstract class AbstractNettyServer implements AutoCloseable
{
	public final Logger log = LoggerFactory.getLogger(getClass());
	protected int port;
	protected int readTimeout = 10;
	protected int idleTimeout = 180;
	protected int writeTimeout = 5;
	protected int maxContentLength = 1048576;
	protected boolean shortCnn = true;
	protected EventLoopGroup bossGroup;
	protected EventLoopGroup workerGroup;
	protected ServerBootstrap bootstrap = new ServerBootstrap();
	protected ChannelFuture channel;
	@Autowired(required = false)
	protected UUID uuid = new TimeMillisUUID(0);

	public AbstractNettyServer()
	{
	}

	public AbstractNettyServer(int port)
	{
		this.port = port;
	}

	@PostConstruct
	public void start() throws Exception
	{
		log.info("start netty sever port:{}", port);
		if (port == 0) return;
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		bootstrap();
		channel = bootstrap.bind(port).sync();
		// channel.channel().closeFuture().sync();
	}

	public abstract void bootstrap() throws Exception;

	// @PreDestroy // spring context×Ô¶¯¹Ø±ÕAutoCloseable
	public void close()
	{
		log.info("shutdown netty server port({}) gracefully...", port);
		if (channel != null) channel.channel().close();
		if (workerGroup != null) workerGroup.shutdownGracefully();
		if (bossGroup != null) bossGroup.shutdownGracefully();
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setReadTimeout(int readTimeout)
	{
		this.readTimeout = readTimeout;
	}

	public void setWriteTimeout(int writeTimeout)
	{
		this.writeTimeout = writeTimeout;
	}

	public void setMaxContentLength(int maxContentLength)
	{
		this.maxContentLength = maxContentLength;
	}

	public void setIdleTimeout(int idleTimeout)
	{
		this.idleTimeout = idleTimeout;
	}

	public void setShortCnn(boolean shortCnn)
	{
		this.shortCnn = shortCnn;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public EventLoopGroup getWorkerGroup()
	{
		return workerGroup;
	}

	public void setWorkerGroup(EventLoopGroup workerGroup)
	{
		this.workerGroup = workerGroup;
	}

	public int getPort()
	{
		return port;
	}

	public int getReadTimeout()
	{
		return readTimeout;
	}

	public int getIdleTimeout()
	{
		return idleTimeout;
	}

	public int getWriteTimeout()
	{
		return writeTimeout;
	}

	public int getMaxContentLength()
	{
		return maxContentLength;
	}

	public boolean isShortCnn()
	{
		return shortCnn;
	}

	public UUID getUuid()
	{
		return uuid;
	}
}
