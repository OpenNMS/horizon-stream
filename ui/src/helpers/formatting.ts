export const formatItemBgColor = (list: [any]) => list.map(item => {
  const bg = {
    ok: 'bg-ok',
    unknown: 'bg-unknown',
    failed: 'bg-failed'
  }
  
  let statusClass = bg.ok
  if(item?.status === 'DOWN') statusClass = bg.failed
  else if(item?.status === 'UNKNOWN') statusClass = bg.unknown

  let latencyClass = bg.ok
  if(item?.icmp_latency === 'FAILED') latencyClass = bg.failed
  else if(item?.icmp_latency === 'UNKNOWN') latencyClass = bg.unknown

  let uptimeClass = bg.ok
  if(item?.snmp_uptime === 'FAILED') uptimeClass = bg.failed
  else if(item?.snmp_uptime === 'UNKNOWN') uptimeClass = bg.unknown

  return {
    ...item,
    statusClass,
    latencyClass,
    uptimeClass
  }
})
