package eu.arrowhead.client.modbus.slave.config;

import java.util.ArrayList;
import java.util.List;

public class SlaveMultiRemoteIO {
	public class Range {
		private int start;
		private int end;
		
		Range(){}
		
		Range(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
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
	
	public class RemoteIOData {
		private String address;
		private int port;
		private int offset;
		private List<Range> ranges;
		
		RemoteIOData(){}
		RemoteIOData(String address, int port, int offset, String ranges){
			this.address = address;
			this.port = port;
			this.offset = offset;
			this.ranges = new ArrayList<>();
			String[] rangesArray = ranges.split("-");
			for(String range : rangesArray) {
				String[] position = range.split("~");
				int start = Integer.parseInt(position[0]);
				int end = Integer.parseInt(position[1]);
				this.ranges.add(new Range(start, end));
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
	
	private List<RemoteIOData> remotes;

	public List<RemoteIOData> getRemotes() {
		return remotes;
	}

	public void setRemotes(List<RemoteIOData> remotes) {
		this.remotes = remotes;
	}
	
	SlaveMultiRemoteIO(){}
	
	SlaveMultiRemoteIO(String[] addresses, int[] ports, int[] offsets, String[] ranges){
		remotes = new ArrayList<>();
		int size = addresses.length;
		if (ports.length != size) {
			return;
		}
		if (offsets.length != size) {
			return;
		}
		if (ranges.length != size) {
			return;
		}
		for (int idx = 0; idx < size; idx++){
			remotes.add(new RemoteIOData(addresses[idx], ports[idx], offsets[idx], ranges[idx]));
		}
	}
}
