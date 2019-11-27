package eu.arrowhead.client.modbus.slave.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

@Primary
@Repository
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix="slave")
public class SlaveRemoteIOs {
	private List<RemoteIOData> remoteIOs;
	
	public static class RemoteIOData {
		private String address;
		private int port;
		private int offset;
		private List<Range> ranges;
		
		public static class Range {
			private int start;
			private int end;
			
			public boolean isInRange(int tmp) {
				if (tmp >= start && tmp <= end) {
					return true;
				}
				return false;
			}
			
			public int getStart() {
				return start;
			}
			public void setStart(int start) {
				this.start = start;
			}
			public int getEnd() {
				return end;
			}
			public void setEnd(int end) {
				this.end = end;
			}
		}	
		
		public Range getRange(int tmp) {
			for (Range range : ranges){
				if (range.isInRange(tmp)) {
					return range;
				}
			}
			return null;
		}
		
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public int getOffset() {
			return offset;
		}
		public void setOffset(int offset) {
			this.offset = offset;
		}
		public List<Range> getRanges() {
			return ranges;
		}
		public void setRanges(List<Range> ranges) {
			this.ranges = ranges;
		} 
	}

	public List<RemoteIOData> getRemoteIOs() {
		return remoteIOs;
	}

	public void setRemoteIOs(List<RemoteIOData> remoteIOs) {
		this.remoteIOs = remoteIOs;
	}
}
