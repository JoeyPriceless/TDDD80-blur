def format_datetime(dt):
    return dt.astimezone().isoformat()


def serialize_list(lst):
    return [element.serialize() for element in lst]