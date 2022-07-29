import { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  Date: any;
  Long: any;
  UNREPRESENTABLE: any;
};

export type AlarmAckDtoInput = {
  ticketId?: InputMaybe<Scalars['String']>;
  ticketState?: InputMaybe<Scalars['String']>;
  user?: InputMaybe<Scalars['String']>;
};

export type AlarmCollectionDto = {
  __typename?: 'AlarmCollectionDTO';
  alarms?: Maybe<Array<Maybe<AlarmDto>>>;
  offset?: Maybe<Scalars['Int']>;
  totalCount?: Maybe<Scalars['Int']>;
};

export type AlarmDto = {
  __typename?: 'AlarmDTO';
  ackTime?: Maybe<Scalars['Date']>;
  ackUser?: Maybe<Scalars['String']>;
  affectedNodeCount?: Maybe<Scalars['Int']>;
  applicationDN?: Maybe<Scalars['String']>;
  clearKey?: Maybe<Scalars['String']>;
  count?: Maybe<Scalars['Int']>;
  description?: Maybe<Scalars['String']>;
  firstAutomationTime?: Maybe<Scalars['Date']>;
  firstEventTime?: Maybe<Scalars['Date']>;
  id?: Maybe<Scalars['Int']>;
  ifIndex?: Maybe<Scalars['Int']>;
  ipAddress?: Maybe<Scalars['String']>;
  lastAutomationTime?: Maybe<Scalars['Date']>;
  lastEvent?: Maybe<EventDto>;
  lastEventTime?: Maybe<Scalars['Date']>;
  location?: Maybe<Scalars['String']>;
  logMessage?: Maybe<Scalars['String']>;
  managedObjectInstance?: Maybe<Scalars['String']>;
  managedObjectType?: Maybe<Scalars['String']>;
  mouseOverText?: Maybe<Scalars['String']>;
  nodeId?: Maybe<Scalars['Int']>;
  nodeLabel?: Maybe<Scalars['String']>;
  operatorInstructions?: Maybe<Scalars['String']>;
  ossPrimaryKey?: Maybe<Scalars['String']>;
  parameters?: Maybe<Array<Maybe<EventParameterDto>>>;
  qosAlarmState?: Maybe<Scalars['String']>;
  reductionKey?: Maybe<Scalars['String']>;
  reductionKeyMemo?: Maybe<ReductionKeyMemoDto>;
  relatedAlarms?: Maybe<Array<Maybe<AlarmSummaryDto>>>;
  serviceType?: Maybe<ServiceTypeDto>;
  severity?: Maybe<Scalars['String']>;
  stickyMemo?: Maybe<MemoDto>;
  suppressedBy?: Maybe<Scalars['String']>;
  suppressedTime?: Maybe<Scalars['Date']>;
  suppressedUntil?: Maybe<Scalars['Date']>;
  troubleTicket?: Maybe<Scalars['String']>;
  troubleTicketLink?: Maybe<Scalars['String']>;
  troubleTicketState?: Maybe<Scalars['Int']>;
  type?: Maybe<Scalars['Int']>;
  uei?: Maybe<Scalars['String']>;
  x733AlarmType?: Maybe<Scalars['String']>;
  x733ProbableCause?: Maybe<Scalars['Int']>;
};

export type AlarmSummaryDto = {
  __typename?: 'AlarmSummaryDTO';
  description?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['Int']>;
  label?: Maybe<Scalars['String']>;
  logMessage?: Maybe<Scalars['String']>;
  nodeLabel?: Maybe<Scalars['String']>;
  reductionKey?: Maybe<Scalars['String']>;
  severity?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['Int']>;
  uei?: Maybe<Scalars['String']>;
};

export type DeviceCollectionDto = {
  __typename?: 'DeviceCollectionDTO';
  devices?: Maybe<Array<Maybe<DeviceDto>>>;
  offset?: Maybe<Scalars['Int']>;
  totalCount?: Maybe<Scalars['Int']>;
};

export type DeviceDto = {
  __typename?: 'DeviceDTO';
  createTime?: Maybe<Scalars['Date']>;
  domainName?: Maybe<Scalars['String']>;
  foreignId?: Maybe<Scalars['String']>;
  foreignSource?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['Int']>;
  label?: Maybe<Scalars['String']>;
  labelSource?: Maybe<Scalars['String']>;
  lastEgressFlow?: Maybe<Scalars['Date']>;
  lastIngressFlow?: Maybe<Scalars['Date']>;
  lastPoll?: Maybe<Scalars['Date']>;
  location?: Maybe<LocationDto>;
  netBiosName?: Maybe<Scalars['String']>;
  operatingSystem?: Maybe<Scalars['String']>;
  parentId?: Maybe<Scalars['Int']>;
  snmpCommunityString?: Maybe<Scalars['String']>;
  sysContact?: Maybe<Scalars['String']>;
  sysDescription?: Maybe<Scalars['String']>;
  sysLocation?: Maybe<Scalars['String']>;
  sysName?: Maybe<Scalars['String']>;
  sysOid?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export type DeviceDtoInput = {
  createTime?: InputMaybe<Scalars['Date']>;
  domainName?: InputMaybe<Scalars['String']>;
  foreignId?: InputMaybe<Scalars['String']>;
  foreignSource?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['Int']>;
  label?: InputMaybe<Scalars['String']>;
  labelSource?: InputMaybe<Scalars['String']>;
  lastEgressFlow?: InputMaybe<Scalars['Date']>;
  lastIngressFlow?: InputMaybe<Scalars['Date']>;
  lastPoll?: InputMaybe<Scalars['Date']>;
  location?: InputMaybe<LocationDtoInput>;
  netBiosName?: InputMaybe<Scalars['String']>;
  operatingSystem?: InputMaybe<Scalars['String']>;
  parentId?: InputMaybe<Scalars['Int']>;
  snmpCommunityString?: InputMaybe<Scalars['String']>;
  sysContact?: InputMaybe<Scalars['String']>;
  sysDescription?: InputMaybe<Scalars['String']>;
  sysLocation?: InputMaybe<Scalars['String']>;
  sysName?: InputMaybe<Scalars['String']>;
  sysOid?: InputMaybe<Scalars['String']>;
  type?: InputMaybe<Scalars['String']>;
};

export type EventCollectionDto = {
  __typename?: 'EventCollectionDTO';
  events?: Maybe<Array<Maybe<EventDto>>>;
  offset?: Maybe<Scalars['Int']>;
  totalCount?: Maybe<Scalars['Int']>;
};

export type EventDto = {
  __typename?: 'EventDTO';
  ackTime?: Maybe<Scalars['Date']>;
  ackUser?: Maybe<Scalars['String']>;
  autoAction?: Maybe<Scalars['String']>;
  correlation?: Maybe<Scalars['String']>;
  createTime?: Maybe<Scalars['Date']>;
  description?: Maybe<Scalars['String']>;
  display?: Maybe<Scalars['String']>;
  host?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['Int']>;
  ifIndex?: Maybe<Scalars['Int']>;
  ipAddress?: Maybe<Scalars['String']>;
  label?: Maybe<Scalars['String']>;
  location?: Maybe<Scalars['String']>;
  log?: Maybe<Scalars['String']>;
  logGroup?: Maybe<Scalars['String']>;
  logMessage?: Maybe<Scalars['String']>;
  mouseOverText?: Maybe<Scalars['String']>;
  nodeId?: Maybe<Scalars['Int']>;
  nodeLabel?: Maybe<Scalars['String']>;
  notification?: Maybe<Scalars['String']>;
  operationActionMenuText?: Maybe<Scalars['String']>;
  operatorAction?: Maybe<Scalars['String']>;
  operatorInstructions?: Maybe<Scalars['String']>;
  parameters?: Maybe<Array<Maybe<EventParameterDto>>>;
  pathOutage?: Maybe<Scalars['String']>;
  serviceType?: Maybe<ServiceTypeDto>;
  severity?: Maybe<Scalars['String']>;
  snmp?: Maybe<Scalars['String']>;
  snmpHost?: Maybe<Scalars['String']>;
  source?: Maybe<Scalars['String']>;
  suppressedCount?: Maybe<Scalars['Int']>;
  time?: Maybe<Scalars['Date']>;
  troubleTicket?: Maybe<Scalars['String']>;
  troubleTicketState?: Maybe<Scalars['Int']>;
  uei?: Maybe<Scalars['String']>;
};

export type EventDtoInput = {
  ackTime?: InputMaybe<Scalars['Date']>;
  ackUser?: InputMaybe<Scalars['String']>;
  autoAction?: InputMaybe<Scalars['String']>;
  correlation?: InputMaybe<Scalars['String']>;
  createTime?: InputMaybe<Scalars['Date']>;
  description?: InputMaybe<Scalars['String']>;
  display?: InputMaybe<Scalars['String']>;
  host?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['Int']>;
  ifIndex?: InputMaybe<Scalars['Int']>;
  ipAddress?: InputMaybe<Scalars['String']>;
  label?: InputMaybe<Scalars['String']>;
  location?: InputMaybe<Scalars['String']>;
  log?: InputMaybe<Scalars['String']>;
  logGroup?: InputMaybe<Scalars['String']>;
  logMessage?: InputMaybe<Scalars['String']>;
  mouseOverText?: InputMaybe<Scalars['String']>;
  nodeId?: InputMaybe<Scalars['Int']>;
  nodeLabel?: InputMaybe<Scalars['String']>;
  notification?: InputMaybe<Scalars['String']>;
  operationActionMenuText?: InputMaybe<Scalars['String']>;
  operatorAction?: InputMaybe<Scalars['String']>;
  operatorInstructions?: InputMaybe<Scalars['String']>;
  parameters?: InputMaybe<Array<InputMaybe<EventParameterDtoInput>>>;
  pathOutage?: InputMaybe<Scalars['String']>;
  serviceType?: InputMaybe<ServiceTypeDtoInput>;
  severity?: InputMaybe<Scalars['String']>;
  snmp?: InputMaybe<Scalars['String']>;
  snmpHost?: InputMaybe<Scalars['String']>;
  source?: InputMaybe<Scalars['String']>;
  suppressedCount?: InputMaybe<Scalars['Int']>;
  time?: InputMaybe<Scalars['Date']>;
  troubleTicket?: InputMaybe<Scalars['String']>;
  troubleTicketState?: InputMaybe<Scalars['Int']>;
  uei?: InputMaybe<Scalars['String']>;
};

export type EventParameterDto = {
  __typename?: 'EventParameterDTO';
  name?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
  value?: Maybe<Scalars['String']>;
};

export type EventParameterDtoInput = {
  name?: InputMaybe<Scalars['String']>;
  type?: InputMaybe<Scalars['String']>;
  value?: InputMaybe<Scalars['String']>;
};

export type LocationDto = {
  __typename?: 'LocationDTO';
  geolocation?: Maybe<Scalars['String']>;
  latitude?: Maybe<Scalars['Float']>;
  locationName?: Maybe<Scalars['String']>;
  longitude?: Maybe<Scalars['Float']>;
  monitoringArea?: Maybe<Scalars['String']>;
  priority?: Maybe<Scalars['Int']>;
  tags?: Maybe<Array<Maybe<Scalars['String']>>>;
};

export type LocationDtoInput = {
  geolocation?: InputMaybe<Scalars['String']>;
  latitude?: InputMaybe<Scalars['Float']>;
  locationName?: InputMaybe<Scalars['String']>;
  longitude?: InputMaybe<Scalars['Float']>;
  monitoringArea?: InputMaybe<Scalars['String']>;
  priority?: InputMaybe<Scalars['Int']>;
  tags?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

export type MemoDto = {
  __typename?: 'MemoDTO';
  author?: Maybe<Scalars['String']>;
  body?: Maybe<Scalars['String']>;
  created?: Maybe<Scalars['Date']>;
  id?: Maybe<Scalars['Int']>;
  updated?: Maybe<Scalars['Date']>;
};

export type MinionCollectionDto = {
  __typename?: 'MinionCollectionDTO';
  minions?: Maybe<Array<Maybe<MinionDto>>>;
  offset?: Maybe<Scalars['Int']>;
  totalCount?: Maybe<Scalars['Int']>;
};

export type MinionDto = {
  __typename?: 'MinionDTO';
  id?: Maybe<Scalars['String']>;
  label?: Maybe<Scalars['String']>;
  lastUpdated?: Maybe<Scalars['Date']>;
  location?: Maybe<Scalars['String']>;
  status?: Maybe<Scalars['String']>;
};

/** Mutation root */
export type Mutation = {
  __typename?: 'Mutation';
  addDevice?: Maybe<Scalars['Int']>;
  clearAlarm?: Maybe<Scalars['String']>;
  createEvent?: Maybe<Scalars['Boolean']>;
};


/** Mutation root */
export type MutationAddDeviceArgs = {
  device?: InputMaybe<DeviceDtoInput>;
};


/** Mutation root */
export type MutationClearAlarmArgs = {
  ackDTO?: InputMaybe<AlarmAckDtoInput>;
  id?: InputMaybe<Scalars['Long']>;
};


/** Mutation root */
export type MutationCreateEventArgs = {
  event?: InputMaybe<EventDtoInput>;
};

/** Query root */
export type Query = {
  __typename?: 'Query';
  deviceById?: Maybe<DeviceDto>;
  listAlarms?: Maybe<AlarmCollectionDto>;
  listDevices?: Maybe<DeviceCollectionDto>;
  listEvents?: Maybe<EventCollectionDto>;
  listMinions?: Maybe<MinionCollectionDto>;
  minionById?: Maybe<MinionDto>;
};


/** Query root */
export type QueryDeviceByIdArgs = {
  id?: InputMaybe<Scalars['Int']>;
};


/** Query root */
export type QueryMinionByIdArgs = {
  id?: InputMaybe<Scalars['String']>;
};

export type ReductionKeyMemoDto = {
  __typename?: 'ReductionKeyMemoDTO';
  author?: Maybe<Scalars['String']>;
  body?: Maybe<Scalars['String']>;
  created?: Maybe<Scalars['Date']>;
  id?: Maybe<Scalars['Int']>;
  reductionKey?: Maybe<Scalars['String']>;
  updated?: Maybe<Scalars['Date']>;
};

export type ServiceTypeDto = {
  __typename?: 'ServiceTypeDTO';
  id?: Maybe<Scalars['Int']>;
  name?: Maybe<Scalars['String']>;
};

export type ServiceTypeDtoInput = {
  id?: InputMaybe<Scalars['Int']>;
  name?: InputMaybe<Scalars['String']>;
};

export type AlarmsQueryVariables = Exact<{ [key: string]: never; }>;


export type AlarmsQuery = { __typename?: 'Query', listAlarms?: { __typename?: 'AlarmCollectionDTO', alarms?: Array<{ __typename?: 'AlarmDTO', id?: number | null, description?: string | null, severity?: string | null, lastEventTime?: any | null } | null> | null } | null };

export type ClearAlarmMutationVariables = Exact<{
  id: Scalars['Long'];
  ackDTO: AlarmAckDtoInput;
}>;


export type ClearAlarmMutation = { __typename?: 'Mutation', clearAlarm?: string | null };

export type AddDeviceMutationVariables = Exact<{
  device: DeviceDtoInput;
}>;


export type AddDeviceMutation = { __typename?: 'Mutation', addDevice?: number | null };

export type ListDevicesQueryVariables = Exact<{ [key: string]: never; }>;


export type ListDevicesQuery = { __typename?: 'Query', listDevices?: { __typename?: 'DeviceCollectionDTO', devices?: Array<{ __typename?: 'DeviceDTO', id?: number | null, label?: string | null, createTime?: any | null, location?: { __typename?: 'LocationDTO', latitude?: number | null, longitude?: number | null } | null } | null> | null } | null };

export type CreateEventMutationVariables = Exact<{
  event: EventDtoInput;
}>;


export type CreateEventMutation = { __typename?: 'Mutation', createEvent?: boolean | null };

export type ListMinionsQueryVariables = Exact<{ [key: string]: never; }>;


export type ListMinionsQuery = { __typename?: 'Query', listMinions?: { __typename?: 'MinionCollectionDTO', minions?: Array<{ __typename?: 'MinionDTO', id?: string | null, label?: string | null, status?: string | null, location?: string | null, lastUpdated?: any | null } | null> | null } | null };


export const AlarmsDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"Alarms"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listAlarms"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"alarms"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"description"}},{"kind":"Field","name":{"kind":"Name","value":"severity"}},{"kind":"Field","name":{"kind":"Name","value":"lastEventTime"}}]}}]}}]}}]} as unknown as DocumentNode<AlarmsQuery, AlarmsQueryVariables>;
export const ClearAlarmDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"ClearAlarm"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"id"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"Long"}}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"ackDTO"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"AlarmAckDTOInput"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"clearAlarm"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"id"},"value":{"kind":"Variable","name":{"kind":"Name","value":"id"}}},{"kind":"Argument","name":{"kind":"Name","value":"ackDTO"},"value":{"kind":"Variable","name":{"kind":"Name","value":"ackDTO"}}}]}]}}]} as unknown as DocumentNode<ClearAlarmMutation, ClearAlarmMutationVariables>;
export const AddDeviceDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"AddDevice"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"device"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"DeviceDTOInput"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"addDevice"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"device"},"value":{"kind":"Variable","name":{"kind":"Name","value":"device"}}}]}]}}]} as unknown as DocumentNode<AddDeviceMutation, AddDeviceMutationVariables>;
export const ListDevicesDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"ListDevices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listDevices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"devices"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"label"}},{"kind":"Field","name":{"kind":"Name","value":"createTime"}},{"kind":"Field","name":{"kind":"Name","value":"location"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"latitude"}},{"kind":"Field","name":{"kind":"Name","value":"longitude"}}]}}]}}]}}]}}]} as unknown as DocumentNode<ListDevicesQuery, ListDevicesQueryVariables>;
export const CreateEventDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"mutation","name":{"kind":"Name","value":"CreateEvent"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"event"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"EventDTOInput"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"createEvent"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"event"},"value":{"kind":"Variable","name":{"kind":"Name","value":"event"}}}]}]}}]} as unknown as DocumentNode<CreateEventMutation, CreateEventMutationVariables>;
export const ListMinionsDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"ListMinions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"listMinions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"minions"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"id"}},{"kind":"Field","name":{"kind":"Name","value":"label"}},{"kind":"Field","name":{"kind":"Name","value":"status"}},{"kind":"Field","name":{"kind":"Name","value":"location"}},{"kind":"Field","name":{"kind":"Name","value":"lastUpdated"}}]}}]}}]}}]} as unknown as DocumentNode<ListMinionsQuery, ListMinionsQueryVariables>;